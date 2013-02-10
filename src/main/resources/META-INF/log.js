// Override the print function so that the messages go to commons logging
print = function(message) {
    Packages.org.apache.commons.logging.LogFactory.getLog('rhino').debug(message);
};