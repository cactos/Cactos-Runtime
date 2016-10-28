var cpuLine = null,
    memLine = null,
    fsLine = null,
    fsstoLine = null,
    netLine = null;

/*
(function () {
    'use strict';

    var app = angular.module('AjaxApp', []);

    app.controller("singleVMController", function($timeout, $scope, $http, $log){
        var usrURL = document.URL;
        // TODO offset for url
        var url = '/api/monitoring/ajax/virtual' + usrURL.substr(usrURL.lastIndexOf('/') + 0);
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

//update Charts
var chartUpdate = function(e) {
    if(cpuLine!=null){
        cpuLine.data.datasets[0].data = e.data.hardware.cpu_vm;
        cpuLine.update();
    }
    if(memLine!=null){
        memLine.data.datasets[0].data = e.data.hardware.ram_percentage;
        memLine.data.datasets[1].data = e.data.hardware.ram_available;
        memLine.update();
    }
    if(fsLine!=null){
        fsLine.data.datasets[0].data = e.data.storage.disk_read;
        fsLine.data.datasets[1].data = e.data.storage.disk_write;
        fsLine.update();
    }
    if(fsstoLine!=null){
        fsstoLine.data.datasets[0].data = e.data.storage.disk_used;
        fsstoLine.data.datasets[1].data = e.data.storage.disk_available;
        fsstoLine.update();
    }
    if(netLine!=null){
        netLine.data.datasets[0].data = e.data.network.network;
        netLine.update();
    }
};

//$(document).ready(function(){
var buildSingleVM = function () {

    //init values
    var label = Array(60).fill('');
    var initArr = Array(60).fill(0);

    var vmcdata = {
        labels: label,
        datasets: [
            {
                label: 'used %',
                borderColor: "#8dc63f",
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
                borderColor: "#8dc63f",
                data: initArr,
                pointRadius: 0.1
            },{
                label: 'available',
                borderColor: "#2b333d",
                data: initArr,
                pointRadius: 0.1
            }
        ]
    };
    var fsdata = {
        labels: label,
        datasets: [
            {
                label: 'MB/s read',
                borderColor: "#8dc63f",
                data: initArr,
                pointRadius: 0.1
            },{
                label: 'MB/s write',
                borderColor: "#2b333d",
                data: initArr,
                pointRadius: 0.1
            }
        ]
    };
    var fsstodata = {
        labels: label,
        datasets: [
            {
                label: 'MB used',
                borderColor: "#8dc63f",
                data: initArr,
                pointRadius: 0.1
            },{
                label: 'MB available',
                borderColor: "#2b333d",
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
                borderColor: "#8dc63f",
                data: initArr,
                pointRadius: 0.1
            }
        ]
    };

    var options = {
        animation: false,
        animations: {
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
                stacked: true
            }]
        },
        tooltips: {
        }
    };

    //create Charts
    var ctx = document.getElementById("cpuLineChart").getContext("2d");
    cpuLine  = new Chart(ctx, {
        type: 'line',
        data: vmcdata,
        options: options
    });
    var ctx = document.getElementById("memLineChart").getContext("2d");
    memLine  = new Chart(ctx, {
        type: 'line',
        data: memdata,
        options: options
    });
    var ctx = document.getElementById("fsLineChart").getContext("2d");
    fsLine  = new Chart(ctx, {
        type: 'line',
        data: fsdata,
        options: options
    });
    var ctx = document.getElementById("fsstoLineChart").getContext("2d");
    fsstoLine  = new Chart(ctx, {
        type: 'line',
        data: fsstodata,
        options: options
    });
    var ctx = document.getElementById("netLineChart").getContext("2d");
    netLine  = new Chart(ctx, {
        type: 'line',
        data: netdata,
        options: options
    });
//});
};