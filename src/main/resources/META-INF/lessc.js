function lessc(name, input, compress) {
    var result = null;
    var parser = new less.Parser({filename: name});
    parser.parse(input, function(err, tree) {
        if( err == null ) {
            result = tree.toCSS({compress: compress});
        } else {
            throw err;
        }
    });
    return result;
}