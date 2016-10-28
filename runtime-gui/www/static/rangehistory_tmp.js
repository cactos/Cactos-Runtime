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

// clusterOverview Vars for Charts
var clusterCNLine = null,
    clusterPowLine = null,
    clusterVMsLine = null,
    clusterCPULine = null,
    clusterMemLine = null,
    clusterNetLine = null;

// init Array for cluster Overview Data
var clusterCNData = Array(60).fill(0);
var clusterPowData = Array(60).fill(0);
var clusterVMsData = Array(60).fill(0);

var clusterCPUDataTotal = Array(60).fill(0);
var clusterCPUDataSys = Array(60).fill(0);
var clusterCPUDataUsr = Array(60).fill(0);
var clusterCPUDataWio = Array(60).fill(0);

var clusterMemDataFree = Array(60).fill(0);
var clusterMemDataCache = Array(60).fill(0);
var clusterMemDataUsed = Array(60).fill(0);

var clusterNetData = Array(60).fill(0);

/*
(function () {
    'use strict';

    var app = angular.module('AjaxApp', []);

    app.controller("rangeHistoryController", function($timeout, $scope, $http, $log){
        $scope.SendData = function (){
            // loading screen
            $scope.loading = true;

            var date = $('input[name="daterange"]').val();
           
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
                    console.log(response);
                    
                    clusterCNData = response['cns']['cnAmount']

                    initappOverview();

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
        }

        $('#computeNodes').append(createFullView(cnName));

        var label = Array(60).fill('');
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
                        display: false
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
                        display: false
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
                        display: false
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
                        display: false
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
                        display: false
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

        // incerment x for chart Object Array
        x += 1;
    });
} 

function initappOverview(){
    html =
        '<h1>Application Data</h1>'+
        '<div class="col-md-6">'+
            '<li class="computeNode col-md-12">'+
                '<h2>AppData1</h2>'+
                '<hr/>'+
                '<p class="computeNodeData">'+
                '</p>'+
                '<div class="miniCharts">'+
                    '<canvas class="appDataOneLine" id="appDataOne" width="300" height="125"></canvas>'+
                '</div>'+
            '</li>'+
        '</div>'+
        '<div class="col-md-6">'+
            '<li class="computeNode col-md-12">'+
                '<h2>AppData2</h2>'+
                '<hr/>'+
                '<p class="computeNodeData">'+
                '</p>'+
                '<div class="miniCharts">'+
                    '<canvas class="AppDataTwoLine" id="appDataTwo" width="300" height="125"></canvas>'+
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
                '<hr/>'+
                '<p class="computeNodeData">'+
                '</p>'+
                '<div class="miniCharts">'+
                    '<canvas class="clusterCNLine" id="clusterCN" width="300" height="125"></canvas>'+
                '</div>'+
            '</li>'+
        '</div>'+
        '<div class="col-md-6">'+
            '<li class="computeNode col-md-12">'+
                '<h2>Power</h2>'+
                '<hr/>'+
                '<p class="computeNodeData">'+
                '</p>'+
                '<div class="miniCharts">'+
                    '<canvas class="clusterPowLine" id="clusterPow" width="300" height="125"></canvas>'+
                '</div>'+
            '</li>'+
        '</div>'+
        '<div class="col-md-6">'+
            '<li class="computeNode col-md-12">'+
                '<h2>Virtual Machines</h2>'+
                '<hr/>'+
                '<p class="computeNodeData">'+
                '</p>'+
                '<div class="miniCharts">'+
                    '<canvas class="clusterVMLine" id="clusterVM" width="300" height="125"></canvas>'+
                '</div>'+
            '</li>'+
        '</div>'+
        '<div class="col-md-6">'+
            '<li class="computeNode col-md-12">'+
                '<h2>CPU</h2>'+
                '<hr/>'+
                '<p class="computeNodeData">'+
                '</p>'+
                '<div class="miniCharts">'+
                    '<canvas class="clusterCPULine" id="clusterCPU" width="300" height="125"></canvas>'+
                '</div>'+
            '</li>'+
        '</div>'+
        '<div class="col-md-6">'+
            '<li class="computeNode col-md-12">'+
                '<h2>Memory</h2>'+
                '<hr/>'+
                '<p class="computeNodeData">'+
                '</p>'+
                '<div class="miniCharts">'+
                    '<canvas class="clusterMemLine" id="clusterMem" width="300" height="125"></canvas>'+
                '</div>'+
            '</li>'+
        '</div>'+
        '<div class="col-md-6">'+
            '<li class="computeNode col-md-12">'+
                '<h2>Network</h2>'+
                '<hr/>'+
                '<p class="computeNodeData">'+
                '</p>'+
                '<div class="miniCharts">'+
                    '<canvas class="clusterNetLine" id="clusterNet" width="300" height="125"></canvas>'+
                '</div>'+
            '</li>'+
        '</div>';
    $('#clusterDataList').append(html);
}
function initClusterData(){
        var label = Array(60).fill('');
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
                        display: false
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
                        display: false
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
                        display: false
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
                        display: false
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
                        display: false
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
                        display: false
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
function updateClusterCharts(){
    clusterPowLine.clear();
    clusterPowLine.data.datasets[0].data = clusterPowData;
    clusterPowLine.update();
}

function createSingleTile(cn, metric, dateString){
    var startEnd = dateString.split(" - ");
    console.log(startEnd);
                // '<div class="col-md-12">'+
                // '<p class="col-md-4">'+startEnd[0]+'</p>'+
                // '<p class="col-md-4 col-md-offset-4 pull-right">'+startEnd[1]+'</p>'+
                // '</div>'+
    html = 
        '<div class="col-md-6">'+
            '<li class="computeNode col-md-12">'+
                '<h2>'+cn+' '+metric+'</h2>'+
                '<hr/>'+
                '<p class="computeNodeData">'+
                '</p>'+
                '<div class="miniCharts">'+
                    '<canvas class="'+metric+'" id="'+cn+metric+'" width="300" height="225"></canvas>'+
                '</div>'+
            '</li>'+
        '</div>';
    return html;
}

function createFullView(cn){
    
    html = 
        '<div class="col-md-6">'+
            '<li class="computeNode col-md-12">'+
                '<h2>'+cn+' cpu</h2>'+
                '<hr/>'+
                '<p class="computeNodeData">'+
                '</p>'+
                '<div class="miniCharts">'+
                    '<canvas class="cpuLine" id="'+cn+'cpu" width="300" height="125"></canvas>'+
                '</div>'+
            '</li>'+
        '</div>'+
        '<div class="col-md-6">'+
            '<li class="computeNode col-md-12">'+
                '<h2>'+cn+' memory</h2>'+
                '<hr/>'+
                '<p class="computeNodeData">'+
                '</p>'+
                '<div class="miniCharts">'+
                    '<canvas class="memLine" id="'+cn+'mem" width="300" height="125"></canvas>'+
                '</div>'+
            '</li>'+
        '</div>'+
        '<div class="col-md-6">'+
            '<li class="computeNode col-md-12">'+
                '<h2>'+cn+' network</h2>'+
                '<hr/>'+
                '<p class="computeNodeData">'+
                '</p>'+
                '<div class="miniCharts">'+
                    '<canvas class="netLine" id="'+cn+'net" width="300" height="125"></canvas>'+
                '</div>'+
            '</li>'+
        '</div>'+
        '<div class="col-md-6">'+
            '<li class="computeNode col-md-12">'+
                '<h2>'+cn+' power</h2>'+
                '<hr/>'+
                '<p class="computeNodeData">'+
                '</p>'+
                '<div class="miniCharts">'+
                    '<canvas class="powLine" id="'+cn+'pow" width="300" height="125"></canvas>'+
                '</div>'+
            '</li>'+
        '</div>'+
        '<div class="col-md-6">'+
            '<li class="computeNode col-md-12">'+
                '<h2>'+cn+' VMs</h2>'+
                '<hr/>'+
                '<p class="computeNodeData">'+
                '</p>'+
                '<div class="miniCharts">'+
                    '<canvas class="vmLine" id="'+cn+'vms" width="300" height="125"></canvas>'+
                '</div>'+
            '</li>'+
        '</div>';
    return html;
}

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

$('input[name="daterange"]').on('apply.daterangepicker', function(ev, picker){
    var diff = picker.endDate - picker.startDate;
    console.log(diff);
    if (diff < 3600000){
        var start = new Date(picker.endDate - 3600000);
        var end = new Date(picker.endDate);
        var trange = datestring(start)+' - '+datestring(end)
        $('input[name="daterange"]').val(trange);
        alert("Timerange is less than one hour. Minimum Timerange to use is 1 hour. Range set to: "+trange);
    }
});

function datestring(s) {
    var M = ("0" + (s.getMonth() + 1)).slice(-2);
    var D = ("0" + s.getDate()).slice(-2);
    var Y = s.getFullYear();
    var H = s.getHours();
    var m = ("0" + s.getMinutes()).slice(-2);
    var s = ("0" + s.getSeconds()).slice(-2);

    var ds = M+'/'+D+'/'+Y+' '+H+':'+m+':'+s;
    return ds;
}
