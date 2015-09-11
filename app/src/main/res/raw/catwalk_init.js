// Disables alert function.
window.alert = function() {};

var _catwalkAddEventListener = document.addEventListener; 

function _catwalkLimitedEvents(event) {
	limitedEvents = ["webkitvisibilitychange", "visibilitychange"];
	return limitedEvents.indexOf(event) > -1;
}

document.addEventListener = function (event, callback, useCapture) {
    if (!_catwalkLimitedEvents(event)) {
    	_catwalkAddEventListener(event, callback, useCapture);
    }
}