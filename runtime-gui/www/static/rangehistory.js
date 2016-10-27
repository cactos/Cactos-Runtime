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

(function () {
    'use strict';

    var app = angular.module('AjaxApp', []);

    app.controller("rangeHistoryController", function($timeout, $scope, $http, $log){
        $scope.SendData = function (){
            var cns = $('select[name="computenode"]').val()
            console.log(cns);
            if (cns == null) {
                alert("Warning! No Computenode Chosen!");
                return;
            }
            // var cn = 'computenode02';
            var mode = $('select[name="mode"]').val();
            var metric = $('select[name="metric"]').val();
            var date = $('input[name="daterange"]').val();
            
            var chartLines = Array(cns.length);

            var cpuLines = Array(cns.length);
            var memLines = Array(cns.length);
            var netLines = Array(cns.length);
            var powLines = Array(cns.length);
            var vmLines = Array(cns.length);
           
            if ($('#keepGraph').is(':checked')){
            
            } else {
                var node = document.getElementById("computeNodes");
                while (node.hasChildNodes()) {
                    node.removeChild(node.lastChild);
                }
            } 
            
            var clusterPower = Array(60).fill(0);
            for (var x = 0; x < cns.length; x++) {
                var cn = cns[x];
                var turl = '/requestrangehistory/cn/'+cn+'/mode/'+mode+'/metric/'+metric+'/date/'+date;
                $http.get(turl)
                .success(function(response, status){
                    console.log(status);
                    console.log(response);
                    var cnName = response['computenode'];
                   
                    // creates overview for all metrics
                    if (metric == 'all'){
                        


                        $('#computeNodes').append(createFullView(cnName));

                        var label = Array(60).fill('');
                        //cpu section
                            var cdata = {
                                labels: label,
                                datasets: [
                                        {
                                            label: 'total',
                                            borderColor: color_1, 
                                            data: response['data']['cpu']['data']['total'],
                                            pointRadius: 0.1
                                        },{
                                            label: 'sys',
                                            borderColor: color_2,
                                            data: response['data']['cpu']['data']['sys'],
                                            pointRadius: 0.1
                                        },{
                                            label: 'usr',
                                            borderColor: color_3,
                                            data: response['data']['cpu']['data']['wio'],
                                            pointRadius: 0.1

                                        },{
                                            label: 'wio',
                                            borderColor: color_4,
                                            data: response['data']['cpu']['data']['wio'],
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
                                            data: response['data']['memory']['data']['free'],
                                            pointRadius: 0.1
                                        },{
                                            label: 'cache',
                                            borderColor: color_2,
                                            data: response['data']['memory']['data']['cache'],
                                            pointRadius: 0.1
                                        },{
                                            label: 'used',
                                            borderColor: color_3,
                                            data: response['data']['memory']['data']['used'],
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
                                            data: response['data']['network']['data'],
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
                                            data: response['data']['power']['data'],
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

                            // vbm section
                            var vmdata = {
                                labels: label,
                                datasets: [
                                        {
                                            label: 'max VMs',
                                            borderColor: '#689F38', 
                                            data: response['data']['vms']['data'],
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

                            return;
                    }

                    // overview for single metric
                    $('#computeNodes').append(createSingleTile(cnName, metric, date));
                    
                    var label = Array(60).fill('');
                    label[0] = "22-22-22 22:22:22";
                    label[59] = "26-23-22 44:44:44";
                    if (metric == 'vms'){
                        var data = {
                            labels: label,
                            datasets: [
                                    {
                                        label: 'max VMs',
                                        borderColor: '#689F38', 
                                        data: response['data']['vms']['data'],
                                        pointRadius: 0.1
                                    }
                            ]
                        };
                    }
                    
                    if (metric == 'power'){
                        // cluster power
                        var singleCNPower = response['data']['power']['data']
                        for (var i = 0; i < clusterPower.length; i++){
                            clusterPower[i] += singleCNPower[i];
                        }

                        $('#computeNodes').append(createSingleTile('cluster', 'power', date))
                            var clusterpowdata = {
                                labels: label,
                                datasets: [
                                        {
                                            label: 'consumption',
                                            borderColor: '#689F38', 
                                            data: clusterPower,
                                            pointRadius: 0.1
                                        }
                                ]
                            };
                            var clusterpowoptions = {
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
                            var ctx = document.getElementById('cluster'+"power");
                            var clusterPowLine = new Chart(ctx, {
                                type: 'line',
                                data: clusterpowdata,
                                options: clusterpowoptions
                            });
                        var data = {
                            labels: label,
                            datasets: [
                                    {
                                        label: 'consumption',
                                        borderColor: '#689F38', 
                                        data: response['data']['power']['data'],
                                        pointRadius: 0.1
                                    }
                            ]
                        };
                    }
                    if (metric == 'cpu'){
                            var data = {
                                labels: label,
                                datasets: [
                                        {
                                            label: 'total',
                                            borderColor: color_1, 
                                            data: response['data']['cpu']['data']['total'],
                                            pointRadius: 0.1
                                        },{
                                            label: 'sys',
                                            borderColor: color_2,
                                            data: response['data']['cpu']['data']['sys'],
                                            pointRadius: 0.1
                                        },{
                                            label: 'usr',
                                            borderColor: color_3,
                                            data: response['data']['cpu']['data']['wio'],
                                            pointRadius: 0.1

                                        },{
                                            label: 'wio',
                                            borderColor: color_4,
                                            data: response['data']['cpu']['data']['wio'],
                                            pointRadius: 0.1

                                        }
                                    ]
                            };
                    }
                    if (metric == 'memory'){
                            var data = {
                                labels: label,
                                datasets: [
                                        {
                                            label: 'free',
                                            borderColor: color_1, 
                                            data: response['data']['memory']['data']['free'],
                                            pointRadius: 0.1
                                        },{
                                            label: 'cache',
                                            borderColor: color_2,
                                            data: response['data']['memory']['data']['cache'],
                                            pointRadius: 0.1
                                        },{
                                            label: 'used',
                                            borderColor: color_3,
                                            data: response['data']['memory']['data']['used'],
                                            pointRadius: 0.1

                                        }
                                ]
                            };
                    }
                    if (metric == 'network'){
                            var data = {
                                labels: label,
                                datasets: [
                                        {
                                            label: 'used',
                                            borderColor: color_1, 
                                            data: response['data']['network']['data'],
                                            pointRadius: 0.1
                                        }
                                ]
                            };
                    }
                    
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
                                    gridLines: {
                                        display: false
                                    },
                                    stacked: false,
                                    display: false,
                                    ticks: {
                                        autoSkip: true,
                                        maxRotation: 0,
                                        minRotation: 0
                                    }
                                }],
                                yAxes: [{
                                    stacked: false,
                                    scaleLabel: {
                                        display: true,
                                        labelString: response['data'][metric]['ylabel']
                                    }
                                }]
                            },
                            tooltips: {
                            }
                    };
                    
                    var ctx = document.getElementById(cnName+metric);
                    chartLines[x] = new Chart(ctx, {
                        type: 'line',
                        data: data,
                        options: options
                    }); 
                    
                    // $scope.es = response;
                    // chartUpdate(response);
                })
                .error(function(response, status){
                    console.log(status);
                    console.log(response)
                });
            } 
         };
    });
}());

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
