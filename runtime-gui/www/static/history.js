var  dataplayMasters = null,
     dataplayRequests = null,
     molproJobs = null,
     vmLine = null,
     cnLine = null,
     cpuLine = null,
     memLine = null,
     netLine = null,
     powLine = null,
     fsLine = null,
     stoLine = null,
     tpsLine = null;

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

// angular request loop for historyController, MOVED to dashboard/cluster/app.js
/*
(function () {
    'use strict';

    var app = angular.module('AjaxApp', []);

    app.controller("HistoryController", function($timeout, $scope, $http, $log){
        var url = "/ajax";
        var timeout = "";
        var poller = function(){
            $http.get(url).success(function(response){
                $scope.e = response;
                chartUpdate(response);
                timeout = $timeout(poller, 10000);
            });
        };
        poller();
    });
}());
*/

// update charts
var chartUpdate = function(e) {
    // get the charts
    // application metrics
    if(dataplayMasters == null)
	createApplicationCharts(e);
    else
	updateApplicationCharts(e);
    
    // update vms
    if(vmLine!=null){
        vmLine.data.datasets[0].data = e.vms.vms_running;
        vmLine.data.datasets[1].data = e.vms.vms_paused;
        vmLine.data.datasets[2].data = e.vms.vms_shut;
        vmLine.data.datasets[3].data = e.vms.vms_failure; 
        vmLine.update();
    }
    if(cnLine!=null){
        cnLine.data.datasets[0].data = e.cns.cn_running;
        cnLine.data.datasets[1].data = e.cns.cn_paused;
        cnLine.data.datasets[2].data = e.cns.cn_shut;
        cnLine.update();

    }
    // update cpu
    if(cpuLine!=null){
        cpuLine.data.datasets[0].data = e.cpu.cpu_complete;
        cpuLine.data.datasets[1].data = e.cpu.cpu_sys;
        cpuLine.data.datasets[2].data = e.cpu.cpu_usr;
        cpuLine.data.datasets[3].data = e.cpu.cpu_wio; 
        cpuLine.update();
    }
    //update memory
    if(memLine!=null){
        memLine.data.datasets[0].data = e.memory.memfree;
        memLine.data.datasets[1].data = e.memory.memcache;
        memLine.data.datasets[2].data = e.memory.memused; 
        memLine.update();
    }
    // update network
    if(netLine!=null){
        netLine.data.datasets[0].data = e.network.netThrough;
        netLine.update();
    }
    // update power
    if(powLine!=null){
        powLine.data.datasets[0].data = e.power.power_consumption;
        powLine.update();
    }
    // update filesystem
    if(fsLine!=null){
        fsLine.data.datasets[0].data = e.filesystem.used;
        fsLine.update();
    }
    //update storage
    if(stoLine!=null){
        stoLine.data.datasets[0].data = e.storage.disk_kbrs;
        stoLine.data.datasets[1].data = e.storage.disk_kbws;
        stoLine.update();
    }
    if(tpsLine!=null){
        tpsLine.data.datasets[0].data = e.storage.disk_tps;
        tpsLine.update();
    }

}

var createApplicationCharts = function(data){
    var label = Array(60).fill('');
    
    // Dataplay Masters
    var options = {
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

    // set here data from json
    var datasets = [];    
    var rawData = data['appdata']['masters'];
    var colorIndex = 0;
    angular.forEach(rawData, function(val, key) {
        datasets.push(
          {
            label: key,
            borderColor: colorArray[colorIndex % 4], 
            data: val,
            pointRadius: 0.1
          }      
        );
        colorIndex += 1;
    });

    var chartData = {
        labels: label,
        datasets: datasets
    };
    
    var ctx = document.getElementById("dataplayMastersChart");
    dataplayMasters = new Chart(ctx, {
        type: 'line',
        data: chartData,
        options: options
    });
    
    
    // Dataplay Requests
    var options = {
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

    // set here data from json
    var datasets = [];    
    var rawData = data['appdata']['requests'];
    var colorIndex = 0;
    angular.forEach(rawData, function(val, key) {
        datasets.push(
          {
            label: key,
            borderColor: colorArray[colorIndex % 4], 
            data: val,
            pointRadius: 0.1
          }      
        );
        colorIndex += 1;
    });

    var chartData = {
        labels: label,
        datasets: datasets
    };
    
    var ctx = document.getElementById("dataplayRequestsChart");
    dataplayRequests = new Chart(ctx, {
        type: 'line',
        data: chartData,
        options: options
    });    
    
    // Molpro Jobs
    var options = {
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
                        labelString: 'Number of jobs'
                    }
                }]
            },
            tooltips: {
            }
        };
	    
    var datasets = [];
    var rawData = data['appdata']['molpro_jobs'];
    var colorIndex = 0;
    datasets.push({
            label: 'Total',
            borderColor: colorArray[colorIndex % 4], 
            data: data['appdata']['molpro_jobs_total'],
            pointRadius: 0.1
          });
    colorIndex++;
    angular.forEach(rawData, function(val, key) {
        datasets.push(
          {
            label: key,
            borderColor: colorArray[colorIndex % 4], 
            data: val,
            pointRadius: 0.1
          }      
        );
        colorIndex += 1;
    });

    var chartData = {
        labels: label,
        datasets: datasets
    };
    var ctx = document.getElementById("molproJobsChart");
    molproJobs = new Chart(ctx, {
        type: 'line',
        data: chartData,
        options: options
    });    

}

var updateApplicationCharts = function(data){
    var label = Array(60).fill('');

    // masters
    var datasets = [];    
    var rawData = data['appdata']['masters'];
    var colorIndex = 0;
    angular.forEach(rawData, function(val, key) {
        datasets.push(
          {
            label: key,
            borderColor: colorArray[colorIndex % 4], 
            data: val,
            pointRadius: 0.1
          }      
        );
        colorIndex += 1;
    });
    dataplayMasters.data.datasets = datasets;
    dataplayMasters.update();
    
    // requests
    var datasets = [];    
    var rawData = data['appdata']['requests'];
    var colorIndex = 0;
    angular.forEach(rawData, function(val, key) {
        datasets.push(
          {
            label: key,
            borderColor: colorArray[colorIndex % 4], 
            data: val,
            pointRadius: 0.1
          }      
        );
        colorIndex += 1;
    });
    dataplayRequests.data.datasets = datasets;
    dataplayRequests.update();
    
    // molpro
    var datasets = [];
    var rawData = data['appdata']['molpro_jobs'];
    var colorIndex = 0;
    datasets.push({
            label: 'Total',
            borderColor: colorArray[colorIndex % 4], 
            data: data['appdata']['molpro_jobs_total'],
            pointRadius: 0.1
          });
    colorIndex++;
    angular.forEach(rawData, function(val, key) {
        datasets.push(
          {
            label: key,
            borderColor: colorArray[colorIndex % 4], 
            data: val,
            pointRadius: 0.1
          }      
        );
        colorIndex += 1;
    });
    molproJobs.data.datasets = datasets;
    molproJobs.update();    
        
}

var buildCharts = function() {
    // default variables
    var label = Array(60).fill('');
    var initArr = Array(60).fill(0);
    
    var vmdata = {
            labels: label,
            datasets: [
                {
                    label: 'running',
                    borderColor: color_1,
                    data: initArr,
                    pointRadius: 0.1 
                },{
                    label: 'paused',
                    borderColor: color_2,
                    data: initArr,
                    pointRadius: 0.1
                },{
                    label: 'shut',
                    borderColor: color_3,
                    data: initArr,
                    pointRadius: 0.1 
                },{
                    label: 'failure',
                    borderColor: color_4,
                    data: initArr,
                    pointRadius: 0.1 
                }

            ]
    };
    var cndata = {
            labels: label,
            datasets: [
                {
                    label: 'running',
                    borderColor: color_1,
                    data: initArr,
                    pointRadius: 0.1 
                },{
                    label: 'paused',
                    borderColor: color_2,
                    data: initArr,
                    pointRadius: 0.1
                },{
                    label: 'shut',
                    borderColor: color_3,
                    data: initArr,
                    pointRadius: 0.1 
                }
            ]
    };
    var cpudata = {
            labels: label,
            datasets: [
                {
                    label: 'total',
                    borderColor: color_1,
                    data: initArr,
                    pointRadius: 0.1 
                },{
                    label: 'sys',
                    borderColor: color_2,
                    data: initArr,
                    pointRadius: 0.1
                },{
                    label: 'usr',
                    borderColor: color_3,
                    data: initArr,
                    pointRadius: 0.1 
                },{
                    label: 'wio',
                    borderColor: color_4,
                    data: initArr,
                    pointRadius: 0.1 
                }

            ]
    };
    var memdata = {
            labels: label,
            datasets: [
                {
                    label: 'free',
                    borderColor: color_1,
                    data: initArr,
                    pointRadius: 0.1 
                },{
                    label: 'cache',
                    borderColor: color_2,
                    data: initArr,
                    pointRadius: 0.1
                },{
                    label: 'used',
                    borderColor: color_3,
                    data: initArr,
                    pointRadius: 0.1 
                }
                
            ]
    };
    var netdata = {
            labels: label,
            datasets: [
                {
                    label: 'usage',
                    borderColor: color_1,
                    data: initArr,
                    pointRadius: 0.1 
                }

            ]
    };
    var powdata = {
            labels: label,
            datasets: [
                {
                    label: 'consumption',
                    borderColor: color_1,
                    data: initArr,
                    pointRadius: 0.1
                }

            ]
    };
    var fsdata = {
            labels: label,
            datasets: [
                {
                    label: 'used',
                    borderColor: color_1,
                    data: initArr,
                    pointRadius: 0.1 
                }                   
            ]
    };
    var stodata = {
            labels: label,
            datasets: [
                {
                    label: 'kbr/s',
                    borderColor: color_1,
                    data: initArr,
                    pointRadius: 0.1
                },{
                    label: 'kbw/s',
                    borderColor: color_2,
                    data: initArr,
                    pointRadius: 0.1
                }                    
            ]
    };
    var tpsdata = {
            labels: label,
            datasets: [
                {
                    label: 'tps',
                    borderColor: color_1,
                    data: initArr,
                    pointRadius: 0.1
                }                   
            ]
    };
     var options = {
                animation: {
                    duration: 0
                },
                responsive: true,
                maintainAspectRatio: false,
                responsiveAnimationDuration: 0,
                legend: {
                    display: true
                },
                scales:{
                    xAxes: [{ 
                        stacked: false,
                        display: false
                    }],
                    yAxes: [{
                        stacked: false,
                    }]
                },
                tooltips: {
                }
            };

    // create all charts
    var ctx = document.getElementById("vmLineChart");
    vmLine = new Chart(ctx, {
        type: 'line',
        data: vmdata,
        options: {
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
                    display: false,
                    scaleLabel: {
                        display: true,
                        labelString: 'Last 10 Minutes'
                    }
                }],
                yAxes: [{
                    stacked: false,
                    scaleLabel:{
                        display: true,
                        labelString: 'Amount'
                    }
                }]
            },
            tooltips: {
            }
        }
    });
    var ctx = document.getElementById("cnLineChart");
    cnLine = new Chart(ctx, {
        type: 'line',
        data: cndata,
        options: {
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
                    scaleLabel:{
                        display: true,
                        labelString: 'Amount'
                    }
                }]
            },
            tooltips: {
            }
        }
    });    var ctx = document.getElementById("cpuLineChart").getContext("2d");
    cpuLine = new Chart(ctx, {
        type: 'line',
        data: cpudata,
        options: {
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
                    scaleLabel:{
                        display: true,
                        labelString: 'Percentage'
                    }
                }]
            },
            tooltips: {
            }
        }
    });
    var ctx = document.getElementById("memLineChart").getContext("2d");
    memLine = new Chart(ctx, {
        type: 'line',
        data: memdata,
        options: {
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
                    scaleLabel:{
                        display: true,
                        labelString: 'GB'
                    }
                }]
            },
            tooltips: {
            }
        }
    });
    var ctx = document.getElementById("netLineChart").getContext("2d");
    netLine = new Chart(ctx, {
        type: 'line',
        data: netdata,
        options: {
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
                    scaleLabel:{
                        display: true,
                        labelString: 'MB/s'
                    }
                }]
            },
            tooltips: {
            }
        }
    });
    var ctx = document.getElementById("powLineChart").getContext("2d");
    powLine = new Chart(ctx, {
        type: 'line',
        data: powdata,
        options: {
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
                    scaleLabel:{
                        display: true,
                        labelString: 'Watts'
                    }
                }]
            },
            tooltips: {
            }
        }
    });
    var ctx = document.getElementById("fsLineChart").getContext("2d");
    fsLine = new Chart(ctx, {
        type: 'line',
        data: fsdata,
        options: {
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
                    scaleLabel:{
                        display: true,
                        labelString: 'GB'
                    }
                }]
            },
            tooltips: {
            }
        }
    });
    var ctx = document.getElementById("stoLineChart").getContext("2d");
    stoLine = new Chart(ctx, {
        type: 'line',
        data: stodata,
        options: {
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
                    scaleLabel:{
                        display: true,
                        labelString: 'kb/s'
                    }
                }]
            },
            tooltips: {
            }
        }
    });
    var ctx = document.getElementById("tpsLineChart").getContext("2d");
    tpsLine = new Chart(ctx, {
        type: 'line',
        data: tpsdata,
        options: {
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
                    scaleLabel:{
                        display: true,
                        labelString: 'tps'
                    }
                }]
            },
            tooltips: {
            }
        }
    });

}
