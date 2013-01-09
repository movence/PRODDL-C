/*
 * Copyright J. Craig Venter Institute, 2011
 *
 * The creation of this program was supported by the U.S. National
 * Science Foundation grant 1048199 and the Microsoft allocation
 * in the MS Azure cloud.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Created with IntelliJ IDEA.
 * User: hkim
 * Date: 10/3/12
 * Time: 10:18 AM
 * To change this template use File | Settings | File Templates.
 */

var proddl = (function() {
    /**
     * common code snippets
     * @type {Object}
     */
    var snip = {
        div: '<div>$c$</div>',
        c_div: '<div class="$cl$" style="$s$">$c$</div>',
        r_div: '<div class="row-fluid">$c$</div>',
        errDiv: '<div class="ui-state-error" style="padding: 0 .7em;margin-top: 15px;display:none">' +
                '    <p><span class="ui-icon ui-icon-alert" style="float: left; margin-right: .3em;"></span>'+
                '    <strong>$s$</strong> $m$ </p>'+
                '</div>',
        loadingDiv: '<div id="loadingDiv" style="z-index:1000; position:relative;">' +
                    '   <div id="loadingImageDiv"><p><img src="/resources/images/loading_1.gif" /></p></div>' +
                    '</div>'
    };

    function setIframe() {
        $('iframe').height($(window).height() * .8);
        //$('iframe').width("100%");
        //setInterval(setIframeSize, 5);
    }

    function log() {
        console.log(arguments);
    }

    /**
     * method for making rest request
     * @param t type - post|get
     * @param a address - pdl/r/..
     * @param d data - json format
     * @param cb - callback function
     */
    function makeRest(t, a, d, cb) {
        $.ajax({
            type:t.toUpperCase(),
            url: "/pdl/r/"+a,
            cache: false,
            dataType: 'json',
            data: d,
            processData: false,
            success: function (json) {
                cb(json);
            },
            error: function () {
                log("fail");
            },
            complete: function() { }
        });
    }

    function showLoading(d_n) { $('div#'+d_n).append(snip.loadingDiv); }
    function hideLoading() { $("div#loadingDiv").remove(); }

    /**
     * job tab
     * @type {Object}
     */
    var jobs = {
        getList: function() {
            showLoading('jobMain');
            makeRest('get','joblist','',this.doTables);
        },
        doTables: function(json) {
            $('div#jobMain').html('<table cellpadding="0" cellspacing="0" border="0" class="display" id="jobTable"></table>');
            var jt_d = {
                "bProcessing" : true,
                "bAutoWidth": true,
                "sDom": 'lfrtip',
                //sPaginationType: "full_numbers",
                "aoColumns": [{'sTitle':'Name'}, {'sTitle':'Job ID'}, {'sTitle':'Status'}],
                "aaData": []
            };
            $.each(json.job, function(i,v) {
                jt_d.aaData.push(v.split(':'));
            });

            $('table#jobTable').dataTable(jt_d);
            hideLoading();
        }
    };

    /**
     * file tab
     */
    var files = function() {
        this.tn = 'fileMain';
        this.init();
    };
    files.prototype.init = function() {
        $('div#fileMain').html(
            '<div id="accordion">' +
            '   <div>'+
            '       <h2><a href="#">List</a></h2><div id="f_l"></div>'+
            '   </div>'+
            '   <div>'+
            '       <h2><a href="#">Upload</a></h2><div id="f_u"></div>'+
            '   </div>'+
            '</div>'
        );
        this.f_list('f_l');
        this.f_upload('f_u');
        $("div#fileMain #accordion").accordion({
            header: "h2",
            heightStyle: "content",
            autoHeight: false
        });
    };
    files.prototype.f_list = function(d_n) {
        showLoading('f_l');
        var r_cb = function(json) {
            $('div#f_l').html('<table cellpadding="0" cellspacing="0" border="0" class="display" id="fileTable"></table>');
            var jt_d = {
                "bProcessing" : true,
                "bAutoWidth": true,
                "sDom": 'lfrtip',
                "aoColumns": [{'sTitle':'ID'}, {'sTitle':'Name'}, {'sTitle':'Status'}],
                "aaData": []
            };
            $.each(json.file, function(i,v) {
                jt_d.aaData.push(v.split(':'));
            });

            $('table#fileTable').dataTable(jt_d);
            hideLoading();
        };
        makeRest('get','filelist','',r_cb);
    };
    files.prototype.f_upload = function(d_n) {
        $('div#'+d_n).append(
            '<form id="fileuploadForm" action="/pdl/r/file/upload" method="POST" enctype="multipart/form-data" class="cleanform">' +
            '   <input id="file" type="file" name="file"/>'+
            '   <button type="submit">Upload</button>'+
            '</form>'+
            '<div class="progress"><div class="bar"></div ><div class="percent">0%</div ></div>'+
            '<div id="status" style="display:hidden"></div>'
        );
        var bar=$('.bar'), percent=$('.percent'), status=$('#status');
        $('form').ajaxForm({
            beforeSend: function() {
                status.empty();
                var percentVal = '0%';
                bar.width(percentVal)
                percent.html(percentVal);
            },
            uploadProgress: function(event, position, total, percentComplete) {
                var percentVal = percentComplete + '%';
                bar.width(percentVal)
                percent.html(percentVal);
            },
            complete: function(xhr) {
                status.html(xhr.responseText);
            }
        });
    };

    /**
     * admin tab
     */
    var admin = function() {
        this.tn = 'adminMain';
        this.init();
        /*
         $('div#adminMain').html('<iframe src="/pdl/w/admin/main" id="adminIframe" width="90%" frameborder="0" scrolling="no"></iframe>');
         setIframe();
         */
    };
    admin.prototype.init = function() {
        var adminObj = this;
        var draw = function(d) {
            if(d['hinder']!=null && d['hinder']===true) {
                $('div#adminMain')/*.height($(document).height())*/.html(
                    '<div id="accordion">' +
                    '   <div>'+
                    '       <h2><a href="#">Cloud Management</a></h2><div id="a_cm"></div>'+
                    '   </div>'+
                    '   <div>'+
                    '       <h2><a href="#">User Management</a></h2><div id="a_um"></div>'+
                    '   </div>'+
                    '</div>'
                );
                adminObj.c_manage('a_cm'); //add cloud management content
                adminObj.u_manage('a_um'); //add user management content
                var curr;
                $("div#adminMain #accordion").accordion({
                    header: "h2",
                    create: function(e, ui) {
                        curr = $('h3[aria-expanded="true"][aria-selected="true"] > a').text().trim();
                    },
                    change: function(e, ui) {
                        curr = ui.newHeader[0].innerText.trim();
                        if(curr==='Cloud Management') {

                        }
                    },
                    heightStyle: "content",
                    autoHeight: false
                });
            } else { //non-admin user
                $('div#adminMain').html(snip.errDiv.replace('$s$', 'Access Denied').replace('$m$', 'You do not have permission'));
            }
        };
        makeRest('get','role','',draw);
    };
    admin.prototype.c_manage = function(d_n) {
        var r_cb = function(json) {
            var msg;
            if($('div#res_cm').length>0)
                $.remove('div#res_cm');
            if(json!=null && json.result!=null && json.result==='submitted') {
                msg='<div class="ui-state-highlight ui-corner-all" id="res_cm">'+json.result+'</div>';
            } else {
                msg='<div class="ui-state-error ui-corner-all" id="res_cm">Update failed. Try again it later.</div>';
            }
            $('div#'+d_n).append(msg);
        };
        var c_m_cb = function(d) {
            if(d.max!=null && d.c_c!=null) {
                var max= parseInt(d.max), c_c= parseInt(d.c_c);
                var opts='';
                for(var i=1;i<max;i++) {
                    opts+='<option value='+i+'>'+i+'</option>'
                }
                $('div#'+d_n)
                    .append(snip.r_div.replace( //instance selector
                        '$c$',
                        '<div class="span2">Worker Instance</div><div><select id="i_c">'+opts+'</select></div>'
                    ))
                    .append(snip.r_div.replace('$c$', //buttons
                        '<p>' +
                            '<div class="span2 offset2" style="text-align: right;">' +
                                '<button class="btn btn-primary" type="button" style="margin-left:15px;" id="c_b_u">Update</a>' +
                            '</div>' +
                        '</p>'));
                $('button#c_b_u').click(function() { //update button
                    if(parseInt($('select#i_c').val())!==c_c) {
                        makeRest('post', 'job/scale', JSON.stringify({'n_worker':$('select#i_c').val()}), r_cb);
                    }
                });
                $('select#i_c').val(c_c);
                hideLoading();
                //$('div#'+d_n).css('height', '').css('overflow', 'none');
            } else {
                $('div#'+d_n).html(snip.errDiv.replace('$s$', 'Error').replace('$m$', 'Please try it again later.'));
            }
        };
        showLoading(d_n);
        makeRest('get', 'instance', null, c_m_cb);
    };
    admin.prototype.u_manage = function(d_n) {
        var validation = function() {
            return $('input#u_id').val()!=null && $('input#u_id').val().length>0 && $('input#u_pw').val()!=null && $('input#u_pw').val().length>0;
        };
        var r_cb = function(json) {
            var msg;
            $.remove('div#res_um');
            if(json!=null && json.result!=null && json.result.indexOf('added')>=0) {
                $('div#'+d_n+' input').val('');
                msg='<div class="ui-state-highlight ui-corner-all" id="res_um">'+json.result+'</div>';
            } else {
                msg='<div class="ui-state-error ui-corner-all" id="res_um">Adding user failed.</div>';
            }
            $(msg).insertBefore($('div#hd_ui'));
        };
        $('div#'+d_n)
            .append('<div id="hd_ui"><h4>User Information</h3></div>')
            .append(snip.r_div.replace('$c$', '<div class="span1">User ID</div><div class="span2"><input type="text" id="u_id"/></div>'))
            .append(snip.r_div.replace('$c$', '<div class="span1">Password</div><div class="span2"><input type="text" id="u_pw"/></div>'))
            .append(snip.r_div.replace( //first, last name
                '$c$',
                '<div class="span1">First Name</div><div class="span2"><input type="text" id="u_fn"/></div>' +
                    '<div class="span1 offset1">Last Name</div><div class="span2"><input type="text" id="u_ln"/></div>'
                )
            )
            .append(snip.r_div.replace( //admin selector
                '$c$',
                '<div class="span1">Admin</div><div class="span2"><select id="u_r"><option value="0">No</option><option value="1">Yes</option></select></div>'
            ))
            .append(snip.r_div.replace('$c$', //buttons
                '<p>' +
                    '<div class="span3" style="text-align: left;">' +
                        '<button class="btn btn-primary disabled" type="button" id="u_b_a">Add User</a>' +
                        '<button class="btn btn-warning" type="button" style="margin-left:15px;" id="u_b_c">Clear</a>' +
                    '</div>' +
                '</p>'));

        $('input#u_id, input#u_pw').change(function() { //button trigger
            if(val()) {
                $('button#u_b_a').removeClass('disabled');
            } else {
                $('button#u_b_a').addClass('disabled');
            }
        });
        $('button#u_b_c').click(function() {//clear button
            $('div#'+d_n+' input').val('');
        });
        $('button#u_b_a').click(function() { //user add button
            if(validation()) {
                var data = {};
                data['id']=$('input#u_id').val();
                data['password']=$('input#u_pw').val();
                data['firstname']=$('input#u_fn').val();
                data['lastname']=$('input#u_ln').val();
                data['admin']=$('select#u_r').val();
                makeRest('post', 'job/adduser', JSON.stringify(data), r_cb);
            }
        });
    };

    /**
     * main method which draws tabbed div areas
     */
    var run = function() {
        this.tn = 'mainTabs';
        this.tabs = ['job', 'file', 'admin'];
        this.draw();
        this.cpr();
    };

    run.prototype.draw = function() {
        var li='', divs='';
        $.each(this.tabs, function(i, v) {
            li +='<li><a href="#'+v+'">'+ v.charAt(0).toUpperCase()+ v.substr(1)+'</a></li>'
            divs += '<div id="'+v+'"><div id="'+v+'Main"></div></div>';
        });
        $('div#main').html('<div id="'+this.tn+'"><ul>'+li+'</ul>'+divs+'</div>');

    };
    run.prototype.cpr = function(){
        var tn = '#'+this.tn;
        $(tn).tabs();
        $(tn).bind('tabsshow', function(e, ui) {
            switch(ui.tab.text.toLowerCase()) {
                case 'job':
                    jobs.getList();
                    break;
                case 'file':
                    new files();
                    break;
                case 'admin':
                    new admin();
                    break;
                default:
                    return;
            }
        });
        $(tn).tabs('select', 1);
    };

    return {
        run: run
    }
})();