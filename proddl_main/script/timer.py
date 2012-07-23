import os, sys, getopt, socket, time, datetime, glob, shlex

def doLogging(fileName):
    currTime = time.time()
    logPath = os.path.join(os.curdir, fileName)
    
    '''remove if log file already exists'''
    if os.path.exists(logPath) and os.path.isfile(logPath):
        os.remove(logPath)
        
    tempLog = os.path.join(os.curdir, fileName+'.temp')
    if os.path.exists(tempLog) and os.path.isfile(tempLog):
        startTime = 0.0
        inFile = open(tempLog, "r")
        while inFile:
            line =  inFile.readline()
            if line is not None and len(line)>0:
                startTime=float(line)
                break
        inFile.close()
                   
        logFile = open(logPath, "w")
        logFile.write('[worker] {0}, {1}, {2}, {3}'.format(socket.gethostname(), startTime, currTime, datetime.timedelta(seconds=currTime-startTime)))
        logFile.close() 
        
        os.remove(tempLog)
        return 1   
    else:
        tempFile = open(tempLog, "w")
        tempFile.write(str(currTime))
        tempFile.close()
        return 0
    
def doMerging(fileName):
    instances = []
    isTemp=doLogging(fileName)
    if isTemp==1:
        logPath = os.path.join(os.curdir, fileName)
        logFile = open(logPath, 'r')
        finalLog = None
        while logFile:
            line=logFile.readline()
            if line is not None and len(line)>0:
                finalLog = line
                break
        logFile.close()
        
        logFile = open(logPath, 'w')
        if finalLog is not None and len(finalLog)>0:
            timeLog = ''
            for afile in glob.glob(os.path.join(os.curdir, '*.log')):
                if afile!=logPath and 'final' not in afile:
                    inFile = open(afile, 'r')
                    """
                    if 'final' in afile:
                        for line in inFile:
                            logFile.write(line)
                    else:
                    """
                    while inFile:
                        line=inFile.readline()
                        if line is not None and len(line)>0 and line!='\n':
                            timeLog+=line+'\n'
                            #logFile.write(line + '\n')
                            iname = shlex.split(line)[0]
                            if instances.count(iname) == 0:
                                instances.append(iname)
                            break
                    inFile.close()
                    os.remove(afile)
            #timeLog+='\nNumber of Worker instance used: '+str(len(instances))
            logFile.write('\n'+timeLog+'"worker count":'+str(len(instances)))
            #logFile.write('\nNumber of Worker instance used: '+str(len(instances)))
        logFile.close()
                
    
def main():
    # parse command line options
    try:
        opts, args = getopt.getopt(sys.argv[1:], "h", ["help"])
    except getopt.error, msg:
        print msg
        print "for help use --help"
        sys.exit(2)
    # process options
    for o, a in opts:
        if o in ("-h", "--help"):
            print __doc__
            sys.exit(0) 
    
    fileName = 'job.log'
    master = False
    if args is not None and len(args) == 2:
        master = 'true'==args[0]
        fileName = args[1]
    
    if master==True:
        doMerging(fileName)
    else:
        doLogging(fileName)
        
if __name__ == "__main__":
    main()    