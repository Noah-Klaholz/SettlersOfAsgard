const CACHE_NAME = 'settlers-asgard-v1';
const STATIC_ASSETS = [
    '/',
    '/style.css',
    '/script.js',
    '/images/map_prelook.png',
    '/images/game-logo.png'
];

self.addEventListener('install', event => {
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(cache => cache.addAll(STATIC_ASSETS))
    );
});

self.addEventListener('fetch', event => {
    event.respondWith(
        caches.match(event.request)
            .then(response => response || fetch(event.request))
    );
});
