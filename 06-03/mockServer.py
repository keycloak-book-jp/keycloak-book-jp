import sys
import http.server as s
 
LISTEN_ADDRESS = '127.0.0.1'
LISTEN_PORT = int(sys.argv[1])
 
class ServerHandler(s.BaseHTTPRequestHandler):
 
    def do_GET(self):
        self.send_response(200)
        self.send_header('Content-type', 'text/html')
        self.end_headers()
        self.wfile.write("{} {} {}<br>\n".format(self.command, self.path, self.request_version).encode())
        for h in self.headers:
            self.wfile.write("{}: {}<br>\n".format(h, self.headers[h]).encode())
 
httpd = s.HTTPServer((LISTEN_ADDRESS, LISTEN_PORT), ServerHandler)
httpd.serve_forever()

