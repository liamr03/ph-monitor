const CACHE_NAME = 'ph-monitor-cache-v1';
const STATIC_ASSETS = [
    '/',
    '/index.html',
    '/icon.png',
    '/manifest.json',
];

// Install: cache files
self.addEventListener('install', event => {
    event.waitUntil(
        caches.open(CACHE_NAME).then(cache => {
            return cache.addAll(STATIC_ASSETS);
        })
    );
});

// Activate: cleanup old caches
self.addEventListener('activate', event => {
    event.waitUntil(
        caches.keys().then(keys =>
            Promise.all(
                keys.filter(key => key !== CACHE_NAME).map(key => caches.delete(key))
            )
        )
    );
});

// Fetch: serve from cache or fetch from network
self.addEventListener('fetch', event => {
    if (event.request.method !== 'GET') return;

    event.respondWith(
        caches.match(event.request).then(cacheRes => {
            return (
                cacheRes ||
                fetch(event.request).catch(() =>
                    new Response('Offline', { status: 503, statusText: 'Offline' })
                )
            );
        })
    );
});
