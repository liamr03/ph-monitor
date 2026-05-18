const CACHE_NAME = 'ph-monitor-cache-v1';
const STATIC_ASSETS = [
    '/',
    '/index.html',
    '/icon.png',
    '/manifest.json',
    '/service-worker.js',
];

// API routes that should never be cached
const API_ROUTES = ['/ph'];

// Install: cache static assets
self.addEventListener('install', event => {
    event.waitUntil(
        caches.open(CACHE_NAME).then(cache => {
            return cache.addAll(STATIC_ASSETS);
        }).then(() => self.skipWaiting())
    );
});

// Activate: cleanup old caches and take control immediately
self.addEventListener('activate', event => {
    event.waitUntil(
        Promise.all([
            caches.keys().then(keys =>
                Promise.all(
                    keys.filter(key => key !== CACHE_NAME).map(key => caches.delete(key))
                )
            ),
            self.clients.claim()
        ])
    );
});

// Fetch: different strategies for static vs API
self.addEventListener('fetch', event => {
    const url = new URL(event.request.url);

    // API endpoints: network-only, never cache
    if (API_ROUTES.some(route => url.pathname.startsWith(route))) {
        return;
    }

    // Static assets: cache-first strategy
    if (event.request.method !== 'GET') return;

    event.respondWith(
        caches.match(event.request).then(cacheRes => {
            if (cacheRes) {
                return cacheRes;
            }

            return fetch(event.request).then(networkRes => {
                // Clone and cache successful responses
                if (networkRes.status === 200) {
                    const responseClone = networkRes.clone();
                    caches.open(CACHE_NAME).then(cache => {
                        cache.put(event.request, responseClone);
                    });
                }
                return networkRes;
            }).catch(() => {
                // Fallback for offline
                if (event.request.destination === 'document') {
                    return caches.match('/');
                }
                return new Response('Offline', { status: 503, statusText: 'Offline' });
            });
        })
    );
});
