var cpuLine = null,
    memLine = null,
    netLine = null,
    powLine = null,
    stoLine = null;

var fsLines = [],
    fsCharts = [],
    fsStoLines = [],
    fsStoCharts = [],
    stoRWLines = [],
    stoRWCharts = [],
    stoTPSLines = [],
    stoTPSCharts = [];

// vmvars
var cpuLinesVM = [],
    cpuChartsVM = [],
    memLinesVM = [],
    memChartsVM = [],
    fsLinesVM = [],
    fsChartsVM = [];

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

function createVarsVM(c, mode){
    for (var i = 0; i < c.length; i++){
        if (mode === 'cpu')
           cpuLinesVM[i] = $(c[i]).attr('id');
       if (mode === 'mem') 
           memLinesVM[i] = $(c[i]).attr('id');
       if (mode === 'fs')
           fsLinesVM[i] = $(c[i]).attr('id');
    }
}

function createVars(c, mode){
    for (var i = 0; i < c.length; i++){
        if (mode === 'fs')
           fsLines[i] = $(c[i]).attr('id');
        if (mode === 'fssto')
           fsStoLines[i] = $(c[i]).attr('id');
        if (mode === 'storw')
            stoRWLines[i] = $(c[i]).attr('id');
        if (mode === 'stotps')
            stoTPSLines[i] = $(c[i]).attr('id');
    }
}

// angular request for singleCNController
/*
(function () {
    'use strict';

    var app = angular.module('AjaxAppCN', []);

    app.controller("singleCNController", function($timeout, $scope, $http, $log){
        var usrURL = document.URL;
        var url = usrURL.substr(usrURL.lastIndexOf('/') + 0);
        var timeout = "";
        var poller = function(){
            $http.get(url).success(function(response){
                $scope.es = response;
                chartUpdate(response);
                timeout = $timeout(poller, 10000);
            });
        };
        // var url2 = usrURL.substr(usrURL.lastIndexOf('/') + 0);
        var url2 ="/api/monitoring/ajax/virtual" + url;
        var timeout2 = "";
        var poller2 = function(){
            $http.get(url2).success(function(response2){
                $scope.e = response2;
                chartUpdateVM(response2);
                timeout2 = $timeout(poller2, 10000);
            });
        };
        poller();
        poller2();
    });
}());
*/

var chartUpdateVM = function(e){
    for (var i = 0; i < e.length; i++){
        var vm = e[i];
        var name = vm.virtualmachine;

        //update cpu
        if(cpuLinesVM.length > 0){
            cpuChartsVM[i].data.datasets[0].data = vm.data.hardware.cpu_vm;
            cpuChartsVM[i].update();
        }
        if(memLinesVM.length > 0){
            memChartsVM[i].data.datasets[0].data = vm.data.hardware.ram_used;
            memChartsVM[i].data.datasets[1].data = vm.data.hardware.ram_available;
            memChartsVM[i].update();
        }
        if(fsLinesVM.length > 0){
            fsChartsVM[i].data.datasets[0].data = vm.data.storage.disk_used;
            fsChartsVM[i].data.datasets[1].data = vm.data.storage.disk_available;
            fsChartsVM[i].update();
        }
    }
};


// update charts
var chartUpdate = function(es){
    //update cpu
    if(cpuLine!=null){
        cpuLine.data.datasets[0].data = es.data.cpu.cpu_sys;
        cpuLine.data.datasets[1].data = es.data.cpu.cpu_usr;
        cpuLine.data.datasets[2].data = es.data.cpu.cpu_wio;
        cpuLine.data.datasets[3].data = es.data.cpu.cpu_complete;
        cpuLine.update();
    }
    if(memLine!=null){
        memLine.data.datasets[0].data = es.data.memory.memused;
        memLine.data.datasets[1].data = es.data.memory.memcache;
        memLine.data.datasets[2].data = es.data.memory.memfree;
        memLine.update();
    }
    if(netLine!=null){
        netLine.data.datasets[0].data = es.data.network.netThrough;
        netLine.update();
    }
    if(powLine!=null){
        powLine.data.datasets[0].data = es.data.power.power_consumption;
        powLine.update();
    }
    if(fsLines.length > 0){
        for ( var i = 0; i < fsLines.length; i++){
            fsCharts[i].data.datasets[0].data = es.data.filesystem["writemax_"+i];
            fsCharts[i].data.datasets[1].data = es.data.filesystem["readmax_"+i];
            fsCharts[i].update();
        }
    }
    if(fsStoLines.length > 0){
        for (var i = 0; i < fsStoLines.length; i++){
            fsStoCharts[i].data.datasets[0].data = es.data.filesystem["used_"+i];
            fsStoCharts[i].data.datasets[1].data = es.data.filesystem["available_"+i];
            fsStoCharts[i].update();
        }
    }
    if(stoRWLines.length > 0){
        for ( var i = 0; i < stoRWLines.length; i++){
            stoRWCharts[i].data.datasets[0].data = es.data.storage["disk_kbrs_"+i];
            stoRWCharts[i].data.datasets[1].data = es.data.storage["disk_kbws_"+i];
            stoRWCharts[i].update();
        }
    }
    if(stoTPSLines.length > 0){
        for ( var i = 0; i < stoTPSLines.length; i++){
            stoTPSCharts[i].data.datasets[0].data = es.data.storage["disk_tps_"+i];
            stoTPSCharts[i].update();
        }
    }
};

//$(document).ready(function(){
var buildSingleCN = function () {

    var fs = document.getElementsByClassName('fsChartsCN');
    var fsSto = document.getElementsByClassName('fsStoChartsCN');
    var storw = document.getElementsByClassName('storwChartsCN');
    var stotps = document.getElementsByClassName('stotpsChartsCN');
    createVars(fs, 'fs');
    createVars(fsSto, 'fssto');
    createVars(storw, 'storw');
    createVars(stotps, 'stotps');

    var label = Array(60).fill('');
    var initArr = Array(60).fill(0);

    var cdata = {
        labels: label,
        datasets: [
            {
                label: 'system',
                borderColor: color_1,
                data: initArr,
                pointRadius: 0.1
            }, {
                label: 'user',
                borderColor: color_2,
                data: initArr,
                pointRadius: 0.1
            }, {
                label: 'wio',
                borderColor: color_3,
                data: initArr,
                pointRadius: 0.1
            }, {
                label: 'total',
                bordercolor: color_4,
                data: initArr,
                pointRadius: 0.1
            }
        ]
    };
    var mdata = {
        labels: label,
        datasets: [
            {
                label: 'used',
                borderColor: color_1,
                data: initArr,
                pointRadius: 0.1
            }, {
                label: 'cache',
                borderColor: color_2,
                data: initArr,
                pointRadius: 0.1
            }, {
                label: 'free',
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
            }]
    };
    var fsdata = {
        labels: label,
        datasets: [
            {
                label: 'writemax',
                borderColor: color_1,
                data: initArr,
                pointRadius: 0.1
            }, {
                label: 'readmax',
                borderColor: color_2,
                data: initArr,
                pointRadius: 0.1
            }
        ]
    };
    var fsstodata = {
        labels: label,
        datasets: [
            {
                label: 'used',
                borderColor: color_1,
                data: initArr,
                pointRadius: 0.1
            }, {
                label: 'available',
                borderColor: color_2,
                data: initArr,
                pointRadius: 0.1
            }
        ]
    };
    var storwdata = {
        labels: label,
        datasets: [
            {
                label: 'kbr/s',
                borderColor: color_1,
                data: initArr,
                pointRadius: 0.1
            }, {
                label: 'kbw/s',
                borderColor: color_2,
                data: initArr,
                pointRadius: 0.1
            }
        ]
    };
    var stotpsdata = {
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
        scales: {
            xAxes: [{
                stacked: false,
                display: false
            }],
            yAxes: [{
                stacked: false
            }]
        },
        tooltips: {}
    };

    //create Charts
    var ctx = document.getElementById("cpuLineChartCN").getContext("2d");
    cpuLine = new Chart(ctx, {
        type: 'line',
        data: cdata,
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
            scales: {
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
            tooltips: {}
        }
    });
    var ctx = document.getElementById("memLineChartCN").getContext("2d");
    memLine = new Chart(ctx, {
        type: 'line',
        data: mdata,
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
            scales: {
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
            tooltips: {}
        }
    });
    var ctx = document.getElementById("netLineChartCN").getContext("2d");
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
            scales: {
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
            tooltips: {}
        }
    });
    var ctx = document.getElementById("powLineChartCN").getContext("2d");
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
            scales: {
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
            tooltips: {}
        }
    });
    for (var i = 0; i < fsLines.length; i++) {
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
                    }
                },
                scales: {
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
                tooltips: {}
            }
        });
    }
    for (var i = 0; i < fsStoLines.length; i++) {
        var ctx = document.getElementById(fsStoLines[i]);
        fsStoCharts[i] = new Chart(ctx, {
            type: 'line',
            data: fsstodata,
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
                scales: {
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
                tooltips: {}
            }
        });
    }
    for (var i = 0; i < stoRWLines.length; i++) {
        var ctx = document.getElementById(stoRWLines[i]);
        stoRWCharts[i] = new Chart(ctx, {
            type: 'line',
            data: storwdata,
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
                scales: {
                    xAxes: [{
                        stacked: false,
                        display: false
                    }],
                    yAxes: [{
                        stacked: false,
                        scaleLabel: {
                            display: true,
                            labelString: 'kb/s'
                        }
                    }]
                },
                tooltips: {}
            }
        });
    }
    for (var i = 0; i < stoTPSLines.length; i++) {
        var ctx = document.getElementById(stoTPSLines[i]);
        stoTPSCharts[i] = new Chart(ctx, {
            type: 'line',
            data: stotpsdata,
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
                scales: {
                    xAxes: [{
                        stacked: false,
                        display: false
                    }],
                    yAxes: [{
                        stacked: false,
                        scaleLabel: {
                            display: true,
                            labelString: 'tps'
                        }
                    }]
                },
                tooltips: {}
            }
        });
    }

    // vm section
    var cpuVM = document.getElementsByClassName('cpuCharts');
    var memVM = document.getElementsByClassName('memCharts');
    var fsVM = document.getElementsByClassName('fsCharts');

    createVarsVM(cpuVM, 'cpu');
    createVarsVM(memVM, 'mem');
    createVarsVM(fsVM, 'fs');

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
    var vmmemdata = {
        labels: label,
        datasets: [
            {
                label: 'used',
                borderColor: color_1,
                data: initArr,
                pointRadius: 0.1
            }, {
                label: 'available',
                borderColor: color_2,
                data: initArr,
                pointRadius: 0.1
            }
        ]
    };
    var vmfsdata = {
        labels: label,
        datasets: [
            {
                label: 'used',
                borderColor: color_1,
                data: initArr,
                pointRadius: 0.1
            }, {
                label: 'available',
                borderColor: color_2,
                data: initArr,
                pointRadius: 0.1
            }
        ]
    };
    for (var i = 0; i < cpuLinesVM.length; i++) {
        var ctx = document.getElementById(cpuLinesVM[i]);
        cpuChartsVM[i] = new Chart(ctx, {
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
                    }
                },
                scales: {
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
                tooltips: {}
            }
        });
    }
    for (var i = 0; i < memLinesVM.length; i++) {
        var ctx = document.getElementById(memLinesVM[i]);
        memChartsVM[i] = new Chart(ctx, {
            type: 'line',
            data: vmmemdata,
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
                scales: {
                    xAxes: [{
                        stacked: false,
                        display: false
                    }],
                    yAxes: [{
                        stacked: false,
                        scaleLabel: {
                            display: true,
                            labelString: 'MB'
                        }
                    }]
                },
                tooltips: {}
            }
        });
    }
    for (var i = 0; i < fsLinesVM.length; i++) {
        var ctx = document.getElementById(fsLinesVM[i]);
        fsChartsVM[i] = new Chart(ctx, {
            type: 'line',
            data: vmfsdata,
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
                scales: {
                    xAxes: [{
                        stacked: false,
                        display: false
                    }],
                    yAxes: [{
                        stacked: false,
                        scaleLabel: {
                            display: true,
                            labelString: 'MB'
                        }
                    }]
                },
                tooltips: {}
            }
        });
    }

//});
};