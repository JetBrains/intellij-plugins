var http = require('http');
var url = require('url');
var path = require('path');
var fs = require('fs');

http.createServer(function(req, res) {
  var webroot = 'www';
  var uri = webroot + url.parse(req.url).pathname;
  var fileName = path.join(process.cwd(), uri);

  if (uri == 'www/') {
    fileName = path.join(process.cwd(), 'www/index.html');
  }

  fs.readFile(fileName, function(err, file) {
    if(err){
      // 404
      res.writeHead(404, {"Content-Type": "text/plain"});
      res.write('404 not found\n');
      res.end();
    } else {
      // 200
      var mimeType = 'text/plain';// Default mime-type
      var extention = path.extname(fileName);
      if (extention == '.html') {
        mimeType = 'text/html';
      } else if (extention == '.css') {
        mimeType = 'text/css';
      } else if (extention == '.js') {
        mimeType = 'text/javascript';
      }

      res.writeHead(200, {"Content-Type": mimeType});
      res.write(file);
      res.end();
    }
  });

}).listen(1337, '127.0.0.1');

console.log('Server running at http://127.0.0.1:1337/');
