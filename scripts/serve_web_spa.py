#!/usr/bin/env python3
import http.server
import socketserver
import os
import sys

PORT = int(sys.argv[1]) if len(sys.argv) > 1 else 3000
DIRECTORY = os.path.abspath(sys.argv[2]) if len(sys.argv) > 2 else os.path.join(
    os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "mobile", "build", "web"
)

class SPARequestHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=DIRECTORY, **kwargs)

    def do_GET(self):
        # Resolve requested file path
        path = self.translate_path(self.path)
        # If the file doesn't exist on disk, serve index.html for Single Page App routing
        if not os.path.exists(path) and not path.endswith('/'):
            self.path = '/index.html'
        return super().do_GET()

    def end_headers(self):
        # Allow cross-origin access and prevent caching issues
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Cache-Control', 'no-cache, must-revalidate')
        super().end_headers()

class ReusableTCPServer(socketserver.TCPServer):
    allow_reuse_address = True

if __name__ == '__main__':
    if not os.path.exists(DIRECTORY):
        print(f"Directory {DIRECTORY} does not exist yet. Please build Flutter web first.")
        sys.exit(1)

    print(f"🚀 Serving Sanjay Library Web SPA from {DIRECTORY} on 0.0.0.0:{PORT}...")
    with ReusableTCPServer(("0.0.0.0", PORT), SPARequestHandler) as httpd:
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("\nShutting down web server...")
            httpd.server_close()
