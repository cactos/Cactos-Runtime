var darkPrimary = "#689F38",
    primary = "#8BC34A",
    lightPrimary = "#DCEDC8",
    primaryText = "#212121",
    secondaryText = "#757575",
    accentColor = "#607D8B",
    dividerColor = "#BDBDBD";

var color_1 = "#689F38",
    color_2 = "#8BC34A",
    color_3 = "#757575",
    color_4 = "#212121";

var colorArray = [color_1, color_2, color_3, color_4];

// clusterOverview Vars for Charts
var clusterCNLine = null,
    clusterPowLine = null,
    clusterVMsLine = null,
    clusterCPULine = null,
    clusterMemLine = null,
    clusterNetLine = null;

// the labels array        
var label = Array(60).fill(['', '']);

// init Array for cluster Overview Data
var clusterCNData = null;
var clusterPowData = null;
var clusterVMsData = null;

var clusterCPUDataTotal = null;
var clusterCPUDataSys = null;
var clusterCPUDataUsr = null;
var clusterCPUDataWio = null;

var clusterMemDataFree = null;
var clusterMemDataCache = null;
var clusterMemDataUsed = null;

var clusterNetData = null;

/*
(function () {
    'use strict';

    var app = angular.module('AjaxApp', []);

    app.controller("rangeHistoryController", function($timeout, $scope, $http, $log){
        $scope.SendData = function (){
            // loading screen
            $scope.loading = true;


            var date = $('input[name="daterange"]').val();
            createLabelString(date);
            initVars();
           
            // clear content for new request
            var node = document.getElementById("computeNodes");
            while (node.hasChildNodes()) {
                node.removeChild(node.lastChild);
            }
            var appnode = document.getElementById("appDataList");
            while (appnode.hasChildNodes()) {
                appnode.removeChild(appnode.lastChild);
            }
            var clusternode = document.getElementById("clusterDataList");
            while (clusternode.hasChildNodes()) {
                clusternode.removeChild(clusternode.lastChild);
            }
           
            // get cnListhistory from hbase
            $http.get('/getCNListHistory/'+date)
                .success(function(response, status){
                    clusterCNData = response['cns']['cnAmount']

                    initappOverview();
                    initAppData(response['apphistory']);

                    initClusterOverview();

                    initCNOverview(response);
                    initClusterData();
                    
                    $scope.loading = false;
                })
                .error(function(response, status){
                    alert("ERROR! No Data recieved!\n"+"Status: "+status+'\n'+'Response: '+response);
                   $scope.loading = false; 
                });
           
         };
    });
}());
*/


var initDatepicker = function() {
    $('input[name="daterange"]').daterangepicker({
        timePicker: true,
        timePicker24Hour: true,
        timePickerIncrement: 10,
        startDate: moment().subtract(1, 'hour'),
        endDate: moment(),
        maxDate: moment(),
        locale: {
            format: 'MM-DD-YYYY H:mm:ss'
        },
        ranges: {
            'Today': [moment().startOf('day'), moment().endOf('day')],
            'Yesterday': [moment().subtract(1, 'days').startOf('day'), moment().subtract(1, 'days').endOf('day')],
            'Last 48 Hours': [moment().subtract(48, 'hours'), moment().endOf('day')],
            'Last 7 Day': [moment().subtract(6, 'days').startOf('day'), moment().endOf('day')]
        }
    });
}

function initVars(){
// init Array for cluster Overview Data
clusterCNData = Array(60).fill(0);
clusterPowData = Array(60).fill(0);
clusterVMsData = Array(60).fill(0);

clusterCPUDataTotal = Array(60).fill(0);
clusterCPUDataSys = Array(60).fill(0);
clusterCPUDataUsr = Array(60).fill(0);
clusterCPUDataWio = Array(60).fill(0);

clusterMemDataFree = Array(60).fill(0);
clusterMemDataCache = Array(60).fill(0);
clusterMemDataUsed = Array(60).fill(0);

clusterNetData = Array(60).fill(0);
}

function createLabelString(date){
    var dateToUnixTS = date.split(" - ").map(function (date){
        return Date.parse(date);
    }); 

    var start = dateToUnixTS[0];
    var stop = dateToUnixTS[1];
    var diff = stop - start;
    var tsInterval = Math.floor(diff / 6);

    for (var i = 0; i < 7; i++){
       var labelDate = new Date(start + (i * tsInterval));

       var month = labelDate.getMonth() + 1;
       var day = labelDate.getDate();
       var hour = labelDate.getHours();
       var minute = labelDate.getMinutes();

       month = (month < 10 ? "0" : "") + month;
       day = (day < 10 ? "0" : "") + day;
       hour = (hour < 10 ? "0" : "") + hour;
       minute = (minute < 10 ? "0" : "") + minute;

       var labelString = [month+'-'+day, hour+':'+minute]
       var labelindex = i * 9 + (i-1);
       labelindex = (labelindex < 9 ? 0 : labelindex);
       label[labelindex] = labelString;
    }
}

function initAppData(res){
    var jobsTotal = res['molpro']['jobsTotal'];

    var depByStatusAll = res['molpro']['deploymentsByStatus'];
    var depByStatusOK = depByStatusAll['OK'] ? depByStatusAll['OK']: 0;
    var depByStatusError = depByStatusAll['ERROR'] ? depByStatusAll['ERROR']: 0;

    var vmStatesCounter = res['molpro']['vmStatesCounter'];
    var vmFailed = vmStatesCounter['paused'] ? vmStatesCounter['paused'] : 0;
    var vmTerminated = vmStatesCounter['shut'] ? vmStatesCounter['shut'] : 0;
    var vmRunning = vmStatesCounter['running'] ? vmStatesCounter['running'] : 0;
    var molProOnedata = {
        labels: ['Jobs', 'Accepted', 'Rejected', 'Failed', 'Terminated', 'Running'],
        datasets: [
            {
                borderColor: [
                    color_1,
                    color_2,
                    color_3,
                    color_4,
                    color_1,
                    color_2
                ],
                borderWidth: 2,
               data: [jobsTotal, depByStatusOK, depByStatusError, vmFailed, vmTerminated, vmRunning] 
            }    
        ]
    };
    var moloptions = {
            legend: {
                display: false,
                labels: {
                    boxWidth: 20         
                }
            },
            scales:{
                xAxes: [{ 
                    stacked: true,
                }],
                yAxes: [{
                    stacked: false,
                    ticks: {
                        beginAtZero: true
                    }
                }]
            },
        };
    var ctx = document.getElementById("molProOne").getContext("2d");
    var molProOneBar = new Chart(ctx, {
        type: 'bar',
        data: molProOnedata,
        options: moloptions
    });

    /////////////////////////////////////////////////////////////////////
    // Graph 2
    ////////////////////////////////////////////////////////////////////
    
    // set here data from json
    var data = res['molpro']['jobsPerType'];
    var molJobsLabels = ['Total'];
    var molJobsData = [jobsTotal];
    angular.forEach(data, function(val, key) {
        molJobsLabels.push(key);
        molJobsData.push(val);
    });

    var molProTwodata = {
        labels: molJobsLabels,
        datasets: [
            {
                borderColor: [
                    color_1,
                    color_2,
                    color_3,
                    color_4,
                    color_1,
                    color_2,
                    color_3,
                    color_4,
                    color_1
                ],
                borderWidth: 2,
               data: molJobsData 
            }    
        ]
    };
    var ctx = document.getElementById("molProTwo").getContext("2d");
    var molProTwoBar = new Chart(ctx, {
        type: 'bar',
        data: molProTwodata,
        options: moloptions
    });
    
    /////////////////////////////////////////////////////////////////////
    // Graph 3
    ////////////////////////////////////////////////////////////////////
    
    // Mol Pro Line Chart One
    var molProLineOptionsOne = {
            animation: {
                duration: 0
            },
            responsive: true,
            maintainAspectRatio: false,
            responsiveAnimationDuration: 0,
            legend: {
                display: true,
                labels: {
                    boxWidth: 20         
                }
            },
            scales:{
                xAxes: [{ 
                    stacked: false,
                    display: true,
                    ticks: {
                        autoSkip: false,
                        maxRotation: 0,
                        minRotation: 0
                    }
                }],
                yAxes: [{
                    stacked: false,
                    scaleLabel: {
                        display: true,
                        labelString: 'Masters'
                    }
                }]
            },
            tooltips: {
            }
        };

    var molLineDataSet = [];

    // set here data from json
    var masterData = res['dataplay']['masters'];
    
    var colorIndex = 0;
    angular.forEach(masterData, function(val, key) {
        molLineDataSet.push(
          {
            label: key,
            borderColor: colorArray[colorIndex % 4], 
            data: val,
            pointRadius: 0.1
          }      
        );
        colorIndex += 1;
    });

    var molLineDataOne = {
        labels: label,
        datasets: molLineDataSet 
    };
    
    var ctx = document.getElementById("molProThree");
    var mol = new Chart(ctx, {
        type: 'line',
        data: molLineDataOne,
        options: molProLineOptionsOne
    });

    /////////////////////////////////////////////////////////////////////
    // Graph 4
    ////////////////////////////////////////////////////////////////////

    // Mol Pro Line Chart Two
    var graphFourOptions = {
            animation: {
                duration: 0
            },
            responsive: true,
            maintainAspectRatio: false,
            responsiveAnimationDuration: 0,
            legend: {
                display: true,
                labels: {
                    boxWidth: 20         
                }
            },
            scales:{
                xAxes: [{ 
                    stacked: false,
                    display: true,
                    ticks: {
                        autoSkip: false,
                        maxRotation: 0,
                        minRotation: 0
                    }
                }],
                yAxes: [{
                    stacked: false,
                    scaleLabel: {
                        display: true,
                        labelString: 'req/sec'
                    }
                }]
            },
            tooltips: {
            }
        };

    var graphFourDataSet = [];

    // set here data from json
    var masterRequestData = res['dataplay']['requests'];
    
    var colorIndex = 0;
    angular.forEach(masterRequestData, function(val, key) {
        graphFourDataSet.push(
          {
            label: key,
            borderColor: colorArray[colorIndex % 4], 
            data: val,
            pointRadius: 0.1
          }      
        );
        colorIndex += 1;
    });

    var graphFourData = {
        labels: label,
        datasets: graphFourDataSet 
    };
    
    var ctx = document.getElementById("molProFour");
    var mol = new Chart(ctx, {
        type: 'line',
        data: graphFourData,
        options: graphFourOptions
    });
}

function initCNOverview(response){
    $('#computeNodes').append('<h1>Computenodes</h1>');
    var cn = response['cns']['cnList'];
    
    // init Object array for CN charts
    var cpuLines = Array(cn.length);
    var memLines = Array(cn.length);
    var netLines = Array(cn.length);
    var powLines = Array(cn.length);
    var vmLines = Array(cn.length);
    
    var x = 0;
    angular.forEach(cn, function(cnName, key) {
        res = response['history'][cnName]; 
   
        // cluster power
        var singleCNPower = res['power']['data'];
        // cluster network
        var singleCNNetwork = res['network']['data'];
        // cluster cpu
        var singleCNCPUTotal = res['cpu']['data']['total'];
        var singleCNCPUSys = res['cpu']['data']['sys'];
        var singleCNCPUUsr = res['cpu']['data']['usr'];
        var singleCNCPUWio = res['cpu']['data']['wio'];
        // cluster memory
        var singleMemFree = res['memory']['data']['free'];
        var singleMemCache = res['memory']['data']['cache'];
        var singleMemUsed = res['memory']['data']['used'];
        var singleVMData = res['vms']['data'];
        
        for (var i = 0; i < clusterPowData.length; i++){
            clusterPowData[i] += singleCNPower[i];
            clusterNetData[i] += singleCNNetwork[i];
            
            clusterCPUDataTotal[i] += singleCNCPUTotal[i];
            clusterCPUDataSys[i] += singleCNCPUSys[i];
            clusterCPUDataUsr[i] += singleCNCPUUsr[i];
            clusterCPUDataWio[i] += singleCNCPUWio[i];
            
            clusterMemDataFree[i] += singleMemFree[i];
            clusterMemDataCache[i] += singleMemCache[i];
            clusterMemDataUsed[i] += singleMemUsed[i];

            clusterVMsData[i] += singleVMData[i];
        }

        $('#computeNodes').append(createFullView(cnName));

        //cpu section
        var cdata = {
            labels: label,
            datasets: [
                    {
                        label: 'total',
                        borderColor: color_1, 
                        data: res['cpu']['data']['total'],
                        pointRadius: 0.1
                    },{
                        label: 'sys',
                        borderColor: color_2,
                        data: res['cpu']['data']['sys'],
                        pointRadius: 0.1
                    },{
                        label: 'usr',
                        borderColor: color_3,
                        data: res['cpu']['data']['wio'],
                        pointRadius: 0.1

                    },{
                        label: 'wio',
                        borderColor: color_4,
                        data: res['cpu']['data']['wio'],
                        pointRadius: 0.1

                    }
                ]
        };
        var coptions = {
                animation: {
                    duration: 0
                },
                responsive: true,
                maintainAspectRatio: false,
                responsiveAnimationDuration: 0,
                legend: {
                    display: true,
                    labels: {
                        boxWidth: 20         
                    }
                },
                scales:{
                    xAxes: [{ 
                        stacked: false,
                        display: true,
                        ticks: {
                            autoSkip: false,
                            maxRotation: 0,
                            minRotation: 0
                        }
                    }],
                    yAxes: [{
                        stacked: false,
                        scaleLabel: {
                            display: true,
                            labelString: 'Percentage'
                        }
                    }]
                },
                tooltips: {
                }
            };
        var ctx = document.getElementById(cnName+"cpu");
        cpuLines[x] = new Chart(ctx, {
            type: 'line',
            data: cdata,
            options: coptions
        });

        // memory section
        var memdata = {
            labels: label,
            datasets: [
                    {
                        label: 'free',
                        borderColor: color_1, 
                        data: res['memory']['data']['free'],
                        pointRadius: 0.1
                    },{
                        label: 'cache',
                        borderColor: color_2,
                        data: res['memory']['data']['cache'],
                        pointRadius: 0.1
                    },{
                        label: 'used',
                        borderColor: color_3,
                        data: res['memory']['data']['used'],
                        pointRadius: 0.1

                    }
            ]
        };
        var memoptions = {
                animation: {
                    duration: 0
                },
                responsive: true,
                maintainAspectRatio: false,
                responsiveAnimationDuration: 0,
                legend: {
                    display: true,
                    labels: {
                        boxWidth: 20         
                    }
                },
                scales:{
                    xAxes: [{ 
                        stacked: false,
                        display: true,
                        ticks: {
                            autoSkip: false,
                            maxRotation: 0,
                            minRotation: 0
                        }
                    }],
                    yAxes: [{
                        stacked: false,
                        scaleLabel: {
                            display: true,
                            labelString: 'GB'
                        }
                    }]
                },
                tooltips: {
                }
            };
        var ctx = document.getElementById(cnName+"mem");
        memLines[x] = new Chart(ctx, {
            type: 'line',
            data: memdata,
            options: memoptions
        });
       
       // network section 
        var netdata = {
            labels: label,
            datasets: [
                    {
                        label: 'used',
                        borderColor: color_1, 
                        data: res['network']['data'],
                        pointRadius: 0.1
                    }
            ]
        };
        var netoptions = {
                animation: {
                    duration: 0
                },
                responsive: true,
                maintainAspectRatio: false,
                responsiveAnimationDuration: 0,
                legend: {
                    display: true,
                    labels: {
                        boxWidth: 20         
                    }
                },
                scales:{
                    xAxes: [{ 
                        stacked: false,
                        display: true,
                        ticks: {
                            autoSkip: false,
                            maxRotation: 0,
                            minRotation: 0
                        }
                    }],
                    yAxes: [{
                        stacked: false,
                        scaleLabel: {
                            display: true,
                            labelString: 'MB/s'
                        }
                    }]
                },
                tooltips: {
                }
            };
        var ctx = document.getElementById(cnName+"net");
        netLines[x] = new Chart(ctx, {
            type: 'line',
            data: netdata,
            options: netoptions
        });
        
        // power section
        var powdata = {
            labels: label,
            datasets: [
                    {
                        label: 'consumption',
                        borderColor: '#689F38', 
                        data: res['power']['data'],
                        pointRadius: 0.1
                    }
            ]
        };
        var powoptions = {
                animation: {
                    duration: 0
                },
                responsive: true,
                maintainAspectRatio: false,
                responsiveAnimationDuration: 0,
                legend: {
                    display: true,
                    labels: {
                        boxWidth: 20         
                    }
                },
                scales:{
                    xAxes: [{ 
                        stacked: false,
                        display: true,
                        ticks: {
                            autoSkip: false,
                            maxRotation: 0,
                            minRotation: 0
                        }
                    }],
                    yAxes: [{
                        stacked: false,
                        scaleLabel: {
                            display: true,
                            labelString: 'Watts'
                        }
                    }]
                },
                tooltips: {
                }
            };
        var ctx = document.getElementById(cnName+"pow");
        powLines[x] = new Chart(ctx, {
            type: 'line',
            data: powdata,
            options: powoptions
        });

        // vm section
        var vmdata = {
            labels: label,
            datasets: [
                    {
                        label: 'max VMs',
                        borderColor: '#689F38', 
                        data: res['vms']['data'],
                        pointRadius: 0.1
                    }
            ]
        };
        var vmoptions = {
                animation: {
                    duration: 0
                },
                responsive: true,
                maintainAspectRatio: false,
                responsiveAnimationDuration: 0,
                legend: {
                    display: true,
                    labels: {
                        boxWidth: 20         
                    }
                },
                scales:{
                    xAxes: [{ 
                        stacked: false,
                        display: true,
                        ticks: {
                            autoSkip: false,
                            maxRotation: 0,
                            minRotation: 0
                        }
                    }],
                    yAxes: [{
                        stacked: false,
                        scaleLabel: {
                            display: true,
                            labelString: 'Amount'
                        }
                    }]
                },
                tooltips: {
                }
            };
        var ctx = document.getElementById(cnName+"vms");
        vmLines[x] = new Chart(ctx, {
            type: 'line',
            data: vmdata,
            options: vmoptions
        });

        $('#collapse'+cnName).collapse();
        // incerment x for chart Object Array
        x += 1;
    });
} 

function initappOverview(){
    html =
        '<h1>Application Data</h1>'+
        '<div class="col-md-6">'+
            '<li class="computeNode col-md-12">'+
                '<h2>HPC Jobs</h2>'+
                '<p class="computeNodeData">'+
                '</p>'+
                '<div class="miniCharts">'+
                    '<canvas class="appDataOneLine" id="molProOne" width="300" height="125"></canvas>'+
                '</div>'+
            '</li>'+
        '</div>'+
        '<div class="col-md-6">'+
            '<li class="computeNode col-md-12">'+
                '<h2>Molpro</h2>'+
                '<p class="computeNodeData">'+
                '</p>'+
                '<div class="miniCharts">'+
                    '<canvas class="AppDataTwoLine" id="molProTwo" width="300" height="125"></canvas>'+
                '</div>'+
            '</li>'+
        '</div>'+
        '<div class="col-md-6">'+
            '<li class="computeNode col-md-12">'+
                '<h2>DataPlay.masters</h2>'+
                '<p class="computeNodeData">'+
                '</p>'+
                '<div class="miniCharts">'+
                    '<canvas class="AppDataThreeLine" id="molProThree" width="300" height="225"></canvas>'+
                '</div>'+
            '</li>'+
        '</div>'+
        '<div class="col-md-6">'+
            '<li class="computeNode col-md-12">'+
                '<h2>DataPlay.requests</h2>'+
                '<p class="computeNodeData">'+
                '</p>'+
                '<div class="miniCharts">'+
                    '<canvas class="AppDataFourLine" id="molProFour" width="300" height="225"></canvas>'+
                '</div>'+
            '</li>'+
        '</div>';
    $('#appDataList').append(html);
}

function initClusterOverview(){
    html = 
        '<h1>Cluster Overview</h1>'+
        '<div class="col-md-6">'+
            '<li class="computeNode col-md-12">'+
                '<h2>Computenodes</h2>'+
                '<p class="computeNodeData">'+
                '</p>'+
                '<div class="miniCharts">'+
                    '<canvas class="clusterCNLine" id="clusterCN" width="300" height="225"></canvas>'+
                '</div>'+
            '</li>'+
        '</div>'+
        '<div class="col-md-6">'+
            '<li class="computeNode col-md-12">'+
                '<h2>Power</h2>'+
                '<p class="computeNodeData">'+
                '</p>'+
                '<div class="miniCharts">'+
                    '<canvas class="clusterPowLine" id="clusterPow" width="300" height="225"></canvas>'+
                '</div>'+
            '</li>'+
        '</div>'+
        '<div class="col-md-6">'+
            '<li class="computeNode col-md-12">'+
                '<h2>Virtual Machines</h2>'+
                '<p class="computeNodeData">'+
                '</p>'+
                '<div class="miniCharts">'+
                    '<canvas class="clusterVMLine" id="clusterVM" width="300" height="225"></canvas>'+
                '</div>'+
            '</li>'+
        '</div>'+
        '<div class="col-md-6">'+
            '<li class="computeNode col-md-12">'+
                '<h2>CPU</h2>'+
                '<p class="computeNodeData">'+
                '</p>'+
                '<div class="miniCharts">'+
                    '<canvas class="clusterCPULine" id="clusterCPU" width="300" height="225"></canvas>'+
                '</div>'+
            '</li>'+
        '</div>'+
        '<div class="col-md-6">'+
            '<li class="computeNode col-md-12">'+
                '<h2>Memory</h2>'+
                '<p class="computeNodeData">'+
                '</p>'+
                '<div class="miniCharts">'+
                    '<canvas class="clusterMemLine" id="clusterMem" width="300" height="225"></canvas>'+
                '</div>'+
            '</li>'+
        '</div>'+
        '<div class="col-md-6">'+
            '<li class="computeNode col-md-12">'+
                '<h2>Network</h2>'+
                '<p class="computeNodeData">'+
                '</p>'+
                '<div class="miniCharts">'+
                    '<canvas class="clusterNetLine" id="clusterNet" width="300" height="225"></canvas>'+
                '</div>'+
            '</li>'+
        '</div>';
    $('#clusterDataList').append(html);
}
function initClusterData(){
        //computenodes 
        var cndata = {
            labels: label,
            datasets: [
                    {
                        label: 'Total',
                        borderColor: color_1, 
                        data: clusterCNData,
                        pointRadius: 0.1
                    }
            ]
        };
        var cnoptions = {
                animation: {
                    duration: 0
                },
                responsive: true,
                maintainAspectRatio: false,
                responsiveAnimationDuration: 0,
                legend: {
                    display: true,
                    labels: {
                        boxWidth: 20         
                    }
                },
                scales:{
                    xAxes: [{ 
                        stacked: false,
                        display: true,
                        ticks: {
                            autoSkip: false,
                            maxRotation: 0,
                            minRotation: 0
                        }
                    }],
                    yAxes: [{
                        stacked: false,
                        scaleLabel: {
                            display: true,
                            labelString: 'Amount'
                        }
                    }]
                },
                tooltips: {
                }
            };
        var ctx = document.getElementById('clusterCN');
        var clusterCNLine = new Chart(ctx, {
            type: 'line',
            data: cndata,
            options: cnoptions
        });
        
        //power
        var powdata = {
            labels: label,
            datasets: [
                    {
                        label: 'consumption',
                        borderColor: '#689F38', 
                        data: clusterPowData,
                        pointRadius: 0.1
                    }
            ]
        };
        var powoptions = {
                animation: {
                    duration: 0
                },
                responsive: true,
                maintainAspectRatio: false,
                responsiveAnimationDuration: 0,
                legend: {
                    display: true,
                    labels: {
                        boxWidth: 20         
                    }
                },
                scales:{
                    xAxes: [{ 
                        stacked: false,
                        display: true,
                        ticks: {
                            autoSkip: false,
                            maxRotation: 0,
                            minRotation: 0
                        }
                    }],
                    yAxes: [{
                        stacked: false,
                        scaleLabel: {
                            display: true,
                            labelString: 'Watts'
                        }
                    }]
                },
                tooltips: {
                }
            };
        var ctx = document.getElementById("clusterPow");
        clusterPowLine = new Chart(ctx, {
            type: 'line',
            data: powdata,
            options: powoptions
        });
        //vms
        var vmdata = {
            labels: label,
            datasets: [
                    {
                        label: 'max VMs',
                        borderColor: '#689F38', 
                        data: clusterVMsData,
                        pointRadius: 0.1
                    }
            ]
        };
        var vmoptions = {
                animation: {
                    duration: 0
                },
                responsive: true,
                maintainAspectRatio: false,
                responsiveAnimationDuration: 0,
                legend: {
                    display: true,
                    labels: {
                        boxWidth: 20         
                    }
                },
                scales:{
                    xAxes: [{ 
                        stacked: false,
                        display: true,
                        ticks: {
                            autoSkip: false,
                            maxRotation: 0,
                            minRotation: 0
                        }
                    }],
                    yAxes: [{
                        stacked: false,
                        scaleLabel: {
                            display: true,
                            labelString: 'Amount'
                        }
                    }]
                },
                tooltips: {
                }
            };
        var ctx = document.getElementById("clusterVM");
        clusterVMsLine = new Chart(ctx, {
            type: 'line',
            data: vmdata,
            options: vmoptions
        });
        //cpu
        var cdata = {
            labels: label,
            datasets: [
                    {
                        label: 'total',
                        borderColor: color_1, 
                        data: clusterCPUDataTotal,
                        pointRadius: 0.1
                    },{
                        label: 'sys',
                        borderColor: color_2,
                        data: clusterCPUDataSys,
                        pointRadius: 0.1
                    },{
                        label: 'usr',
                        borderColor: color_3,
                        data: clusterCPUDataUsr,
                        pointRadius: 0.1

                    },{
                        label: 'wio',
                        borderColor: color_4,
                        data: clusterCPUDataWio,
                        pointRadius: 0.1

                    }
                ]
        };
        var coptions = {
                animation: {
                    duration: 0
                }, 
                responsive: true,
                maintainAspectRatio: false,
                responsiveAnimationDuration: 0,
                legend: {
                    display: true,
                    labels: {
                        boxWidth: 20         
                    }
                },
                scales:{
                    xAxes: [{ 
                        stacked: false,
                        display: true,
                        ticks: {
                            autoSkip: false,
                            maxRotation: 0,
                            minRotation: 0
                        }
                    }],
                    yAxes: [{
                        stacked: false,
                        scaleLabel: {
                            display: true,
                            labelString: 'Percentage'
                        }
                    }]
                },
                tooltips: {
                }
            };
        var ctx = document.getElementById("clusterCPU");
        clusterCPULine = new Chart(ctx, {
            type: 'line',
            data: cdata,
            options: coptions
        });
        

        //mem
        var memdata = {
            labels: label,
            datasets: [
                    {
                        label: 'free',
                        borderColor: color_1, 
                        data: clusterMemDataFree,
                        pointRadius: 0.1
                    },{
                        label: 'cache',
                        borderColor: color_2,
                        data: clusterMemDataCache,
                        pointRadius: 0.1
                    },{
                        label: 'used',
                        borderColor: color_3,
                        data: clusterMemDataUsed,
                        pointRadius: 0.1

                    }
            ]
        };
        var memoptions = {
                animation: {
                    duration: 0
                },
                responsive: true,
                maintainAspectRatio: false,
                responsiveAnimationDuration: 0,
                legend: {
                    display: true,
                    labels: {
                        boxWidth: 20         
                    }
                },
                scales:{
                    xAxes: [{ 
                        stacked: false,
                        display: true,
                        ticks: {
                            autoSkip: false,
                            maxRotation: 0,
                            minRotation: 0
                        }
                    }],
                    yAxes: [{
                        stacked: false,
                        scaleLabel: {
                            display: true,
                            labelString: 'GB'
                        }
                    }]
                },
                tooltips: {
                }
            };
        var ctx = document.getElementById("clusterMem");
        clusterMemLine = new Chart(ctx, {
            type: 'line',
            data: memdata,
            options: memoptions
        });
        
        //net
        var netdata = {
            labels: label,
            datasets: [
                    {
                        label: 'usage',
                        borderColor: '#689F38', 
                        data: clusterNetData,
                        pointRadius: 0.1
                    }
            ]
        };
        var netoptions = {
                animation: {
                    duration: 0
                },
                responsive: true,
                maintainAspectRatio: false,
                responsiveAnimationDuration: 0,
                legend: {
                    display: true,
                    labels: {
                        boxWidth: 20         
                    }
                },
                scales:{
                    xAxes: [{ 
                        stacked: false,
                        display: true,
                        ticks: {
                            autoSkip: false,
                            maxRotation: 0,
                            minRotation: 0
                        }
                    }],
                    yAxes: [{
                        stacked: false,
                        scaleLabel: {
                            display: true,
                            labelString: 'MB/s'
                        }
                    }]
                },
                tooltips: {
                }
            };
        var ctx = document.getElementById("clusterNet");
        clusterNetLine = new Chart(ctx, {
            type: 'line',
            data: netdata,
            options: netoptions
        });
}

function createFullView(cn){
    
    html =
       '<div class="computeNode" data-toggle="collapse" data-target="#collapse'+cn+'" aria-expanded="true"><h1>'+cn+'    <i class="fa fa-chevron-down"></i></h1></i></div>'+
       '<div id="collapse'+cn+'" class="collapse in" aria-expanded="true">'+ 
            '<div class="col-md-6">'+
                '<li class="computeNode col-md-12">'+
                    '<h2>CPU</h2>'+
                    '<p class="computeNodeData">'+
                    '</p>'+
                    '<div class="miniCharts">'+
                        '<canvas class="cpuLine" id="'+cn+'cpu" width="300" height="225"></canvas>'+
                    '</div>'+
                '</li>'+
            '</div>'+
            '<div class="col-md-6">'+
                '<li class="computeNode col-md-12">'+
                    '<h2>Memory</h2>'+
                    '<p class="computeNodeData">'+
                    '</p>'+
                    '<div class="miniCharts">'+
                        '<canvas class="memLine" id="'+cn+'mem" width="300" height="225"></canvas>'+
                    '</div>'+
                '</li>'+
            '</div>'+
            '<div class="col-md-6">'+
                '<li class="computeNode col-md-12">'+
                    '<h2>Network</h2>'+
                    '<p class="computeNodeData">'+
                    '</p>'+
                    '<div class="miniCharts">'+
                        '<canvas class="netLine" id="'+cn+'net" width="300" height="225"></canvas>'+
                    '</div>'+
                '</li>'+
            '</div>'+
            '<div class="col-md-6">'+
                '<li class="computeNode col-md-12">'+
                    '<h2>Power</h2>'+
                    '<p class="computeNodeData">'+
                    '</p>'+
                    '<div class="miniCharts">'+
                        '<canvas class="powLine" id="'+cn+'pow" width="300" height="225"></canvas>'+
                    '</div>'+
                '</li>'+
            '</div>'+
            '<div class="col-md-6">'+
                '<li class="computeNode col-md-12">'+
                    '<h2>Virtual Machines</h2>'+
                    '<p class="computeNodeData">'+
                    '</p>'+
                    '<div class="miniCharts">'+
                        '<canvas class="vmLine" id="'+cn+'vms" width="300" height="225"></canvas>'+
                    '</div>'+
                '</li>'+
            '</div>'+
        '</div>'+
        '<div class="clearfix">';
    return html;
}


// var options = {
//         animation: {
//             duration: 0
//         },
//         responsive: true,
//         maintainAspectRatio: false,
//         responsiveAnimationDuration: 0,
//         legend: {
//             display: true,
//             labels: {
//                 boxWidth: 20         
//             }
//         },
//         scales:{
//             xAxes: [{
//                 gridLines: {
//                     display: false
//                 },
//                 stacked: false,
//                 display: true,
//                 ticks: {
//                     autoSkip: true,
//                     maxRotation: 0,
//                     minRotation: 0
//                 }
//             }],
//             yAxes: [{
//                 stacked: false,
//                 scaleLabel: {
//                     display: true,
//                     labelString: response['data'][metric]['ylabel']
//                 }
//             }]
//         },
//         tooltips: {
//         }
// };
