from getopt import getopt, error
from sys import argv, exit
from cookielib import CookieJar
from urllib2 import *
from MultipartPostHandler import MultipartPostHandler
from time import gmtime, strftime
from json import loads
import shlex
import sqlite3 as lite
import time
import StringIO

_makeflow_rule_tpl = """\
%(targets)s: %(inputs)s
    %(cmd)s
"""

_worker_cmd_tpl = "$PYTHON timer.py false {ind}.log;$TAR -zcf test.{ind}.tar.gz input.dat;$PYTHON timer.py false {ind}.log"
_timer_input_tpl = "LOCAL c:/cygwin/bin/curl.exe 127.0.0.1/pdl/r/file/get?id={fileId} -o timer.py -u admin:pdlAdmin -X POST"

_task_insert_sql = "INSERT INTO task (job_id,task_id,t_stamp,result,return_status,worker_selection_algorithm,time_task_submit,time_task_finish,time_send_input_start,time_send_input_finish,time_execute_cmd_start,time_execute_cmd_finish,time_receive_output_start,time_receive_output_finish,total_bytes_transferred,total_transfer_time,host,tag,command_line) VALUES ('%(jobid)s','%(taskid)s',%(t_stamp)i,%(result)i,%(return)i,'%(algo)s',%(submit)d,%(finish)f,%(input_s)f,%(input_f)f,%(cmd_s)f,%(cmd_f)f,%(output_s)f,%(output_f)f,%(bytes)f,%(ttime)f,'%(host)s','%(tag)s','%(cmd)s');"
_queue_insert_sql = "INSERT INTO queue (job_id,t_stamp,workers_init,workers_ready,workers_busy,tasks_running,tasks_waiting,tasks_complete,total_tasks_dispatched,total_tasks_complete,total_workers_joined,total_workers_removed,total_bytes_sent,total_bytes_received,efficiency,idle_percentage,avg_capacity,total_workers_connected,workers_by_pool) VALUES ('%(jobid)s',%(t_stamp)d,%(init)i,%(ready)i,%(busy)i,%(run)i,%(wait)i,%(complete)i,%(dispat)i,%(complete)i,%(joined)i,%(removed)i,%(sent)i,%(bytes)f,%(eff)f,%(idle)f,%(cap)d,%(conn)i,%(pool)i);"

_dep_gzip = "GZIP=c:/cygwin/bin/gzip.exe\n"
_dep_tar = "TAR=c:/cygwin/bin/tar.exe\n"
_dep_curl = "CURL=c:/cygwin/bin/curl.exe\n"
_dep_rm = "RM=c:/cygwin/bin/rm.exe\n"
_dep_python = "PYTHON=c:/python/python.exe\n"

class MakeflowWriter(object):
    def __init__(self,out):
        self.out = open(out, "w")
        self.done = set()
        
    def _writeEntry(self,targets,inputs,cmd):
        self.out.write(_makeflow_rule_tpl % dict(
            targets=' '.join(targets),
            inputs=' '.join(inputs),
            cmd=cmd))

    def appendMgtJob(self,task):
        jobId = task.jobId
        if jobId not in self.done:
            inputs = []
            targets = []
            for dep in task.depend:
                if isinstance(dep, Task):
                    inputs += dep.outputs
                else:
                    inputs.append(dep)
            targets = task.outputs
            cmd = task.scriptName
            self._writeEntry(targets=targets,inputs=inputs,cmd=cmd)
            self.done.add(task.jobId)
        
    def appendMgtJobs(self,tasks):
        for task in tasks:
            if isinstance(task, Task):
                self.appendMgtJob(task)
                self.appendMgtJobs(task.depend)
    
    def createJobList(self, iterCount, timerId):
        self.out.write(_dep_gzip)
        self.out.write(_dep_tar)
        self.out.write(_dep_curl)
        self.out.write(_dep_rm)
        self.out.write(_dep_python)
        
        """child jobs"""
        jobs = []        
        """perf log initializer for master""" 
        perfJob = Task()
        perfJob.jobId=iterCount+1
        perfJob.depend.append("$PYTHON")
        perfJob.outputs.append("perf.log.temp")
        perfJob.scriptName = "LOCAL $PYTHON timer.py true perf.log"        
        """timer.py job"""
        timerJob = Task()
        timerJob.jobId=iterCount+2
        timerJob.depend.append("$CURL")
        timerJob.outputs.append("timer.py")
        timerJob.scriptName = "LOCAL $CURL 127.0.0.1/pdl/r/file/get?id={timerId} -o timer.py -u admin:pdlAdmin -X POST".format(timerId=timerId)
        perfJob.depend.append(timerJob)
        jobs.append(perfJob)
        
        default_sub_depend = ["$PYTHON","timer.py","input.dat","$TAR"]
        for cnt in range(iterCount):
            job = Task()
            job.jobId=cnt
            job.depend.extend(default_sub_depend)
            job.outputs.extend(["test."+str(cnt)+".tar.gz", str(cnt)+".log"])
            job.scriptName = _worker_cmd_tpl.format(ind=cnt)
            jobs.append(job)        
        
        """main task"""    
        mainJob = Task()
        mainJob.jobId = iterCount+3
        mainJob.outputs=["output.dat"]
        mainJob.scriptName = "LOCAL $TAR -zcf output.dat input.dat;$RM *.tar.gz;$PYTHON timer.py true perf.log"
        mainJob.depend.extend(jobs)
        mainJob.depend.extend(["$TAR", "$RM", "$PYTHON"])
        
        self.appendMgtJobs([mainJob])
            

    def close(self):
        if self.out:
            self.out.close()
            self.out = None

class Task:
    def __init__(self):
        self.taskId=0
        self.depend = []
        self.outputs = []
        self.scriptName = ''
        
class JobSubmitter:        
    def makeRestCall(self,data):
        try:
            jobname = data['jobname']
            restAddr = '{0}/pdl/r/{1}'.format(data['host'], jobname)
            headers = {"Authorization": "Basic "+data['user'].encode("base64").rstrip(), "Accept": "*/*"}
            
            rdata = None
            if data['method'] == 'post':           
                if 'job' in jobname and '/' in jobname and len(jobname) != jobname.index('/')+1:
                    '''input sanity check for new job submission'''  
                    rdata = data['input']              
                    if not 'script' in rdata or not 'input' in rdata:
                        raise 'file information is not given'       
                    '''values = {} 
                    data = urllib.urlencode(values)'''
                    headers['Content-Type'] = 'application/json'
                elif 'file' in jobname:
                    if 'upload' in jobname:
                        cookies = CookieJar()
                        opener = build_opener(HTTPCookieProcessor(cookies), MultipartPostHandler)
                        rdata = {'file' : open(data['file'], 'rb')}
                        install_opener(opener)        
            
            req = Request(restAddr,rdata,headers)
            
            print strftime("%Y-%m-%d %H:%M:%S", gmtime()) + ' - REST request:"{0}", user:"{1}"'.format(restAddr, data['user'])
            
            returnJson = urlopen(req).read()
            print strftime("%Y-%m-%d %H:%M:%S", gmtime()) + ' ' + returnJson
            return returnJson
        except HTTPError, err:
            if err.code == 404:
                print "HTTPERROR not supported REST api: {0}".format(jobname)
            else:
                print "Unexpected error:", sys.exc_info()[0]
                raise
            return None
        except URLError:
            return None
    
    def testCycle(self,args):
        data = {}
        data['host'] = 'http://{0}'.format(args[0])
        data['user'] = 'admin:pdlAdmin'
        data['jobname'] = ''
        job_count = int(args[1])
        
        jobInput = '{"interpreter":"makeflow", "script":"%s", "input":"%s"}'
        currJobId=None
        timerFile = None
        scriptFile=None
        inputFile=None
        _fileJob = 'file/upload'
        _makeflowJob = 'job/execute'
        
        jobs = ['job/execute', 'job']
        
        fileUpload = 'file/upload'
        if args[2]=='id':
            timerFile=args[3]
            inputFile=args[4]
        else:
            jobs = [fileUpload, fileUpload, fileUpload]+(jobs)
                 
        for i in range(job_count):
            if i>0 and jobs.count(fileUpload)>0:
                jobs = filter(lambda a: a!=fileUpload, jobs)
            for idx, job in enumerate(jobs):
                data['method'] = 'post'
                data['jobname'] = job
                if 'file' in job:
                    fileData = ''
                    if args[2]=='path':
                        if idx==0: 
                            fileData = args[3] #timer.py
                        elif idx==1:
                            makeflowFileName = "test.makeflow" 
                            writer = MakeflowWriter(makeflowFileName)
                            writer.createJobList(5, timerFile) #makeflow job iteration count
                            writer.close()
                            fileData = makeflowFileName
                        else:
                            fileData = args[4] #input
                    data['file']=fileData
                        
                elif 'job' in job:
                    if data.has_key('file'):
                        data.pop('file')
                     
                    if 'job'==job and currJobId is not None:
                        data['method'] = 'get'
                        data['jobname'] = data['jobname'] + '?jid='+currJobId
                    elif 'scale' in job:
                        data['method'] = 'post'
                    else:
                        if scriptFile is not None and inputFile is not None:
                            data['input'] = (jobInput%(scriptFile,inputFile)).encode('utf-8')
                            
                while True:
                    returnData = self.makeRestCall(data)
                    if returnData is not None:
                        returnJson = loads(returnData)
                        keys = returnJson.keys()
                        if 'job' == job:
                            jobInfoJson=returnJson['info']
                            jobInfoKeys=jobInfoJson.keys()
                            if 'status' in jobInfoKeys and 'finished'==jobInfoJson['status']:
                                if 'log' in jobInfoKeys:
                                    logFileId=jobInfoJson['log']
                                    data['jobname']='file/get?id='+logFileId
                                    logData=self.makeRestCall(data)
                                    logger=LogParser(currJobId)
                                    logger.parseLog(logData)
                                break                            
                                
                            elif 'status' in jobInfoKeys and 'failed'==jobInfoJson['status']:
                                print 'job failed'
                                break 
                            else:
                                print 'waiting for the job to finish for 3 minute...'
                                time.sleep(180)   
                        else:
                            if 'execute' in job and 'id' in keys:
                                currJobId = returnJson['id']
                            elif 'file' in job and 'id' in keys:
                                if idx==0:
                                    timerFile = returnJson['id']
                                elif idx==1:
                                    scriptFile = returnJson['id']
                                else:
                                    inputFile = returnJson['id']
                            break;
                        
                
class LogParser:
    def __init__(self, jobId):
        self.jobid=jobId
         
    def test(self):
        data = {}
        data['host'] = 'http://157.55.168.45'
        data['user'] = 'admin:pdlAdmin'
        data['jobname'] = 'file/get?id=f7c2a513-b825-b953-fcf5-9e2f02bb38bb'
        data['method']='get'
        
        submitter = JobSubmitter()
        logData=submitter.makeRestCall(data)
        self.parseLog(logData)
               
    def parseLog(self, logData):
        to_db=[]
        buf = StringIO.StringIO(logData)
        while True:
            line = buf.readline()
            if len(line)==0:
                break
            else:
                lineData = shlex.split(line)
                if 'QUEUE' ==lineData[0]:
                    qData=queueData(self.jobid, lineData)
                    to_db.append(_queue_insert_sql%qData.qMap)
                else:
                    tData=taskData(self.jobid, lineData)
                    to_db.append(_task_insert_sql%tData.tMap)
        self.storeToDB(to_db)
    
    def storeToDB(self, d):
        try:
            con=lite.connect('/Users/hkim/Stuffs/Backups/AZURE/data/proddl_curl.sqlite')
            cur = con.cursor()
            for s_d in d:
                cur.execute(s_d)
            con.commit()
        except lite.Error, e:
            if con:
                con.rollback()
            print "Log Parser ERROR %s" % e.args[0]
        finally:
            if con:
                con.close()

class queueData:
    def __init__(self, jobid, data):
        qMap={
            'jobid':jobid,
            't_stamp':int(data[1]),
            'init':int(data[2]),
            'ready':int(data[3]),
            'busy':int(data[4]),
            'run':int(data[5]),
            'wait':int(data[6]),
            'complete':int(data[7]),
            'dispat':int(data[8]),
            'complete':int(data[9]),
            'joined':int(data[10]),
            'removed':int(data[11]),
            'sent':int(data[12]),
            'bytes':int(data[13]),
            'eff':float('0' if data[14]=='nan' else data[14]),
            'idle':float(data[15]),
            'cap':int(data[16]),
            'conn':int(data[17]),
            'pool':int(data[18])
        }
        self.qMap = qMap
class taskData:
    def __init__(self, jobid, data):
        self.startTime = int(data[6])
        tMap={
            'jobid':jobid,
            'taskid':data[2],
            't_stamp':int(data[1]),
            'result':int(data[3]),
            'return':int(data[4]),
            'algo':data[5],
            'submit':self.startTime,
            'finish':self.massageTime(data[7]),
            'input_s':self.massageTime(data[8]),
            'input_f':self.massageTime(data[9]),
            'cmd_s':self.massageTime(data[10]),
            'cmd_f':self.massageTime(data[11]),
            'output_s':self.massageTime(data[12]),
            'output_f':self.massageTime(data[13]),
            'bytes':float(data[14]),
            'ttime':int(data[15])/1000000.0,
            'host':data[16],
            'tag':data[17],
            'cmd':data[18]
        }
        self.tMap = tMap
    def massageTime(self, t_s):
        return (int(t_s)-self.startTime)/1000000.0        

            
def encodeUni2Byte(dic):
    newDic = {}
    for s in dic.keys():
        newDic[s.encode('utf-8')] = dic[s].encode('utf-8')
    return newDic 
       
def main():
    # parse command line options
    try:
        opts, args = getopt(argv[1:], "h", ["help"])
    except error, msg:
        print msg
        print "for help use --help"
        exit(2)
    # process options
    for o, a in opts:
        if o in ("-h", "--help"):
            print __doc__
            exit(0) 
            
    """ ARGS
    [0] - host
    [1] - iteration count
    [2] - id/path identifier for files
    [3] - timer.py
    [4] - input file
    """
    tester = JobSubmitter()
    tester.testCycle(args)
    
    #logger = LogParser("someID")
    #logger.test()
    
if __name__ == "__main__":
    main()