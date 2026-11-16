// Brilliant Burger HTTP server: Node.js standard library only.
const http = require('node:http');
const fs = require('node:fs');
const path = require('node:path');
const FileDatabase = require('./src/storage/FileDatabase');
const OrderService = require('./src/services/OrderService');

const PORT = Number(process.env.PORT || 8080);
const HOST = process.env.HOST || '127.0.0.1';
const ROOT = __dirname;
const PUBLIC = path.join(ROOT, 'public');
const database = new FileDatabase(path.join(ROOT, 'data'));
const orders = new OrderService(database);
const MIME = { '.html':'text/html; charset=utf-8', '.css':'text/css; charset=utf-8',
  '.js':'text/javascript; charset=utf-8', '.png':'image/png', '.svg':'image/svg+xml',
  '.jpg':'image/jpeg', '.jpeg':'image/jpeg', '.ico':'image/x-icon' };

function sendJson(res, code, value) {
  const data = Buffer.from(JSON.stringify(value));
  res.writeHead(code, { 'Content-Type':'application/json; charset=utf-8', 'Cache-Control':'no-store',
    'X-Content-Type-Options':'nosniff' });
  res.end(data);
}
function readJson(req) {
  return new Promise((resolve, reject) => {
    let bytes = 0; const chunks = [];
    req.on('data', part => {
      bytes += part.length;
      if (bytes > 65536) { reject(new RangeError('Request too large')); req.destroy(); return; }
      chunks.push(part);
    });
    req.on('end', () => {
      try { resolve(JSON.parse(Buffer.concat(chunks).toString('utf8'))); }
      catch { reject(new RangeError('Malformed JSON request')); }
    });
    req.on('error', reject);
  });
}
async function handle(req, res) {
  try {
    const url = new URL(req.url, 'http://localhost');
    const pathname = decodeURIComponent(url.pathname);
    const method = req.method;
    if (pathname === '/api/health' && method === 'GET')
      return sendJson(res, 200, {status:'ok', storage:'JSON files', language:'JavaScript'});
    if (pathname === '/api/menu' && method === 'GET') {
      const recipes = database.getMenu().getItems();
      return sendJson(res, 200, {recipes, total:recipes.length});
    }
    if (/^\/api\/menu\/\d+$/.test(pathname) && method === 'GET') {
      const item = database.getMenu().findItem(Number(pathname.split('/').pop()));
      return sendJson(res, item ? 200 : 404, item || {error:'Item not found'});
    }
    if (pathname === '/api/orders' && method === 'POST') {
      const body = await readJson(req);
      return sendJson(res, 201, orders.createOrder(body?.items));
    }
    if (/^\/api\/orders\/\d+$/.test(pathname) && method === 'GET') {
      const order = database.getOrder(Number(pathname.split('/').pop()));
      return sendJson(res, order ? 200 : 404, order || {error:'Order not found'});
    }
    if (pathname.startsWith('/api/')) return sendJson(res, 404, {error:'Unknown API endpoint'});
    if (method !== 'GET' && method !== 'HEAD') return sendJson(res, 405, {error:'Method not allowed'});
    const requested = pathname === '/' ? '/index.html' : pathname.toLowerCase() === '/cart.html' ? '/Cart.html' : pathname;
    const file = path.resolve(PUBLIC, '.' + requested);
    if (!file.startsWith(PUBLIC + path.sep)) return sendJson(res, 403, {error:'Forbidden'});
    let stat;
    try { stat = await fs.promises.stat(file); } catch { return sendJson(res, 404, {error:'File not found'}); }
    if (!stat.isFile()) return sendJson(res, 404, {error:'File not found'});
    res.writeHead(200, { 'Content-Type':MIME[path.extname(file).toLowerCase()] || 'application/octet-stream',
      'X-Content-Type-Options':'nosniff' });
    if (method === 'HEAD') return res.end();
    fs.createReadStream(file).pipe(res);
  } catch (error) {
    if (error instanceof RangeError || error instanceof SyntaxError || error instanceof URIError)
      return sendJson(res, 400, {error:error.message});
    console.error(error);
    if (!res.headersSent) return sendJson(res, 500, {error:'Internal server error'});
    res.end();
  }
}
if (require.main === module) {
  http.createServer(handle).listen(PORT, HOST, () => console.log(`Brilliant Burger running at http://${HOST}:${PORT}`));
}
module.exports = {handle};
