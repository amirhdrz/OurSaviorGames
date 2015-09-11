//jquery + particles

$(document).ready(function(){
  
    particlesJS('particles-js', {
      particles: {
        color: '#fff',
        color_random: false,
        shape: 'circle', // "circle", "edge" or "triangle"
        opacity: {
          opacity: 1,
          anim: {
            enable: true,
            speed: 1.5,
            opacity_min: 0,
            sync: false
          }
        },
        size: 4,
        size_random: true,
        nb: 100,
        line_linked: {
          enable_auto: true,
          distance: 180,
          color: '#fff',
          opacity: 1,
          width: 0.25,
          condensed_mode: {
            enable: false,
            rotateX: 600,
            rotateY: 600
          }
        },
        anim: {
          enable: true,
          speed: 2.5
        }
      },
      interactivity: {
        enable: true,
        mouse: {
          distance: 200
        },
        detect_on: 'canvas', // "canvas" or "window"
        mode: 'grab', // "grab" of false
        line_linked: {
          opacity: 0.8
        },
        events: {
          onclick: {
            enable: true,
            mode: 'push', // "push" or "remove"
            nb: 3
          },
          onresize: {
            enable: true,
            mode: 'out', // "out" or "bounce"
            density_auto: true,
            density_area: 1000 // nb_particles = particles.nb * (canvas width *  canvas height / 1000) / density_area
          }
        }
      },
      /* Retina Display Support */
      retina_detect: true
    });

  
    var h = $(window).height();
    var canvas = $('#particles-js');
      
    canvas.css('height', h);

    c = document.querySelector("canvas");
    $('div#particles-js canvas').css('height',h);
   
  
});

//Text jumble

function copySwap(id, newStr) {
    var ele = document.getElementById('poem'),
        eleTxt = ele.innerHTML,
        oChrs = [],
        nChrs = [];

    // turn new copy into array
    for (var i = 0; i < newStr.length; i++) {
        nChrs.push(newStr[i]);
    }
    // create spans if there not already there
    if(ele.getElementsByTagName('span').length==0){
        for (var j = 0; j < eleTxt.length; j++) {
            // wrap each char in span
            var s = document.createElement('span');
            s.innerHTML = eleTxt[j];
            oChrs.push(s);
        }
        ele.innerHTML = '';
        for (var k = 0; k < eleTxt.length; k++) {
            // swap old chars w/span'd chars
            ele.appendChild(oChrs[k]);
        }
    } else {
        for (var j = 0; j < ele.getElementsByTagName('span').length; j++) {
            oChrs.push( ele.getElementsByTagName('span')[j] );
        }
    }
    
    // swap characters
    var l = (oChrs.length > nChrs.length) ? oChrs.length : nChrs.length;
    var xtra = (oChrs.length < nChrs.length) ? true : false;
    if(xtra){
        // create xtra spans if needed
        for (var o = 0; o < l-oChrs.length; o++) {
            var newS = document.createElement('span');
                ele.appendChild(newS);
        }
    }
        
    for (var p = 0; p < l; p++) {
        // hold different amount of time b4 starting to swap ea.
        hold(ele.getElementsByTagName('span')[p], nChrs[p]);
    }
      
}

function hold(ele, c){ // delay b4 running charSwap
    var ranTime = Math.random()*1000;
    setTimeout(function(){ charSwap(ele, c);},ranTime);
}

function charSwap(ele, c) {
    var chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz',
        rnum = Math.floor(Math.random() * chars.length),
        ran = chars.substring(rnum, rnum + 1);
    if(ele!=undefined && c!=undefined){
        // swap old w/new
        if(Math.random()>0.5){
            ele.innerHTML = ran;
            ele.style.color = '#211e1e';
            setTimeout(function(){charSwap(ele,c)},delay);
        } else {
            ele.innerHTML = c;
            ele.style.color = '#fff';
        } 
    }
    if(c==undefined){
        if(Math.random()>0.5){
            ele.innerHTML = ran;
            setTimeout(function(){charSwap(ele,c)},delay);
        } else {
            ele.innerHTML = '';
        } 
    }
}

// Poem
var delay = 100;
var picabia = [
    'Web games for your mobile device',
    'Available now for Android 4.4+',
    'Our Savior Games'
];

var next = 0;
setInterval(function nextLine(){
    copySwap('poem', picabia[next]);
    next++; if(next>=picabia.length){next=0;}
  }, 5000
)

 '̵̢̖̜̠̩̭̱̺͇͎͖̍̿͒̈̈́̃̀̒̉ͦͪͮ͆̀͝_'