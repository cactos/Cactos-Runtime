var cpuLines = [],
    cpuCharts = [],
    memLines = [],
    memCharts = [],
    fsLines = [],
    fsCharts = [];

var fsSort = [];

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
        // console.log($(c[i]).attr('id'));
        if (mode === 'cpu')
            cpuLines[i] = $(c[i]).attr('id');
        if ( mode === 'mem')
            memLines[i] = $(c[i]).attr('id');
        if ( mode === 'fs')
             fsLines[i] = $(c[i]).attr('id');

    }
}

// angular request loop for historyController
/*
(function () {
    'use strict';

    var app = angular.module('AjaxApp', []);

    app.controller("ComputenodeController", function($timeout, $scope, $http, $log){
        var url = "/api/monitoring/ajax/computenodes";
        var timeout = "";
        var poller = function(){
            $http.get(url).success(function(response){
                $scope.d = response;
                chartUpdate(response);
                timeout = $timeout(poller, 10000);
            });
        };
        poller();
    });
}());
*/

//update charts
var chartUpdate = function(e) {
    // console.log(cpuLines);
    for (var i = 0; i < e.length; i++){
        var cn = e[i];
        var name = cn.computenode;
        
        //update cpu
        if(cpuLines.length > 0){
            cpuCharts[i].clear();
            cpuCharts[i].data.datasets[0].data = cn.data.cpu.cpu_sys;
            cpuCharts[i].data.datasets[1].data = cn.data.cpu.cpu_usr;
            cpuCharts[i].data.datasets[2].data = cn.data.cpu.cpu_wio;
            cpuCharts[i].data.datasets[3].data = cn.data.cpu.cpu_complete;
            cpuCharts[i].update();
        }

        //update memory
        if(memLines.length > 0){
            memCharts[i].data.datasets[0].data = cn.data.memory.memused;
            memCharts[i].data.datasets[1].data = cn.data.memory.memcache;
            memCharts[i].data.datasets[2].data = cn.data.memory.memfree;
            memCharts[i].update();
        }

        if (fsLines.length > 0){
            for (var j = 0; j < fsSort['fs'+name].length; j++){
                // var used = obj["used_"+j]
                fsSort['fs'+name][j].data.datasets[0].data = cn.data.filesystem["used_"+j];
                fsSort['fs'+name][j].data.datasets[1].data = cn.data.filesystem["available_"+j];
                fsSort['fs'+name][j].update();
            }
        }
    }

}

function sortFsCharts(){
    result = new Array();
    for (var i = 0; i < fsLines.length; i++){
        //get value
        cn_api = fsLines[i].substring(0, fsLines[i].indexOf('c')+13);
        //check if existing prop for this id
        if (!result[cn_api]){
            result[cn_api] = [];
        }
        //get value
        result[cn_api].push(fsCharts[i]);
    }
    fsSort = result;
}

//$(document).ready(function(){
var createComputNodes = function () {

    var a = document.getElementsByClassName('cpuCharts');
    var b = document.getElementsByClassName('memCharts');
    var c = document.getElementsByClassName('fsCharts');
    createVars(a, 'cpu');
    createVars(b, 'mem');
    createVars(c, 'fs');

    // default variables
    var label = Array(60).fill('');
    var initArr = Array(60).fill(0);

    //default chart data
    var cpudata = {
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
    var fsdata = {
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
        tooltips: {
            enabled: false
        }
    };
    for (var i = 0; i < cpuLines.length; i++) {
        var ctx = document.getElementById(cpuLines[i]);
        cpuCharts[i] = new Chart(ctx, {
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
                    },
                    onClick: false
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
    for (var i = 0; i < memLines.length; i++) {
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
                    },
                    onClick: false
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
    sortFsCharts();

//});
};