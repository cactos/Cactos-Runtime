var cpuLines = [],
    cpuCharts = [],
    memLines = [],
    memCharts = [],
    fsLines = [],
    fsCharts = [];

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

function createVars(c, mode){
    for (var i = 0; i < c.length; i++){
        if (mode === 'cpu')
           cpuLines[i] = $(c[i]).attr('id');
       if (mode === 'mem') 
           memLines[i] = $(c[i]).attr('id');
       if (mode === 'fs')
           fsLines[i] = $(c[i]).attr('id');

    }
}

// angular request loop for vm Controller
(function (){
    'use strict';

    var app = angular.module('AjaxApp', []);

    app.controller("VMController", function($timeout, $scope, $http, $log){
        var url ="/api/monitoring/ajax/virtual";
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

//update charts
var chartUpdate = function(e){
    for (var i = 0; i < e.length; i++){
        var vm = e[i];
        var name = vm.virtualmachine;

        //update cpu
        if(cpuLines.length > 0){
            cpuCharts[i].data.datasets[0].data = vm.data.hardware.cpu_vm;
            cpuCharts[i].update();
        }
        if(memLines.length > 0){
            memCharts[i].data.datasets[0].data = vm.data.hardware.ram_used;
            memCharts[i].data.datasets[1].data = vm.data.hardware.ram_available;
            memCharts[i].update();
        }
        if(fsLines.length > 0){
            fsCharts[i].data.datasets[0].data = vm.data.storage.disk_used;
            fsCharts[i].data.datasets[1].data = vm.data.storage.disk_available;
            fsCharts[i].update();
        }
    }
};

$(document).ready(function(){
    var cpu = document.getElementsByClassName('cpuCharts');
    var mem = document.getElementsByClassName('memCharts');
    var fs = document.getElementsByClassName('fsCharts');

    createVars(cpu, 'cpu');
    createVars(mem, 'mem');
    createVars(fs, 'fs');

    //default variables
    var label = Array(60).fill('');
    var initArr = Array(60).fill(0);

    var vmcdata = {
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
    var memdata = {
        labels: label,
        datasets: [
                {
                    label: 'used',
                    borderColor: color_1,
                    data: initArr,
                    pointRadius: 0.1 
                },{
                    label: 'available',
                    borderColor: color_2,
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
                },{
                    label: 'available',
                    borderColor: color_2,
                    data: initArr,
                    pointRadius: 0.1  
                }
            ]
    };
    // the default chart options
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
                stacked: false
            }]
        },
        tooltips: {
            enabled: false
        }
    };
    for (var i = 0; i < cpuLines.length; i++){
        var ctx = document.getElementById(cpuLines[i]);
        cpuCharts[i] = new Chart(ctx, {
                type: 'line',
                data: vmcdata,
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
                        },
                        onClick: false
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
    }
    for(var i = 0; i < memLines.length; i++){
        var ctx = document.getElementById(memLines[i]);
		memCharts[i] = new Chart(ctx, {
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
                        },
                        onClick: false
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
                                labelString: 'MB'
                            }
                        }]
                    },
                    tooltips: {
                    }
                }
            });
    }
    for(var i = 0; i < fsLines.length; i++){
        var ctx = document.getElementById(fsLines[i]);
		fsCharts[i] = new Chart(ctx, {
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
                        },
                        onClick: false
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
                                labelString: 'MB'
                            }
                        }]
                    },
                    tooltips: {
                    }
                }
            });
    }
});
