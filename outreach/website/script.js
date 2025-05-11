document.addEventListener('DOMContentLoaded', () => {

    AOS.init({
        duration: 800,
        offset: 100,
        once: true,
        easing: 'ease-in-out',
    });

    const navLinksContainer = document.getElementById('nav-links');
    const burgerButton = document.getElementById('burger-menu');
    const allNavLinks = document.querySelectorAll('#nav-links li a'); // Select all links

    // Video Modal Elements
    const trailerModal = document.getElementById('trailer-modal');
    const demoModal = document.getElementById('demo-modal');
    const closeTrailerModal = document.getElementById('close-trailer-modal');
    const closeDemoModal = document.getElementById('close-demo-modal');
    const trailerVideo = document.getElementById('trailer-video');
    const demoVideo = document.getElementById('demo-video');
    const trailerFallback = document.getElementById('trailer-fallback-message');
    const demoFallback = document.getElementById('demo-fallback-message');

    // --- Debounce Function ---
    function debounce(func, wait = 15, immediate = false) {
        let timeout;
        return function () {
            const context = this, args = arguments;
            const later = function () {
                timeout = null;
                if (!immediate) func.apply(context, args);
            };
            const callNow = immediate && !timeout;
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
            if (callNow) func.apply(context, args);
        };
    };

    // Toggle nav menu on burger button click
    if (burgerButton && navLinksContainer) {
        burgerButton.addEventListener('click', () => {
            navLinksContainer.classList.toggle('nav-active');
            burgerButton.classList.toggle('active'); // Toggle burger animation class
        });
    }

    // Function to check H.265 support
    function supportsH265(videoElement) {
        // Common H.265 codecs: hvc1 (common) or hev1
        return videoElement.canPlayType('video/mp4; codecs="hvc1"') || videoElement.canPlayType('video/mp4; codecs="hev1"');
    }

    // Function to open a modal
    function openModal(modal, video, fallbackMessage) {
        if (!supportsH265(video)) {
            video.style.display = 'none';
            fallbackMessage.style.display = 'block';
        } else {
            video.style.display = 'block';
            fallbackMessage.style.display = 'none';
            video.play();
        }
        modal.classList.add('show');
        // Close mobile nav if open
        if (navLinksContainer.classList.contains('nav-active')) {
            navLinksContainer.classList.remove('nav-active');
            burgerButton.classList.remove('active');
        }
    }

    // Function to close a modal
    function closeModal(modal, video) {
        modal.classList.remove('show');
        video.pause();
        video.currentTime = 0; // Reset video
    }

    // Event listeners for all navigation links
    allNavLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            const href = link.getAttribute('href');
            const linkId = link.id;

            if (linkId === 'watch-trailer-link') {
                e.preventDefault();
                openModal(trailerModal, trailerVideo, trailerFallback);
            } else if (linkId === 'watch-demo-link') {
                e.preventDefault();
                openModal(demoModal, demoVideo, demoFallback);
            } else if (href && href.startsWith('#')) {
                e.preventDefault();
                const targetId = href;
                const targetElement = document.querySelector(targetId);

                if (targetElement) {
                    const headerOffset = document.querySelector('.sticky-nav').offsetHeight;
                    const elementPosition = targetElement.getBoundingClientRect().top;
                    const offsetPosition = elementPosition + window.pageYOffset - headerOffset;

                    window.scrollTo({
                        top: offsetPosition,
                        behavior: "smooth"
                    });
                }
                // Close nav menu when a link is clicked (for mobile view)
                if (navLinksContainer.classList.contains('nav-active')) {
                    navLinksContainer.classList.remove('nav-active');
                    burgerButton.classList.remove('active');
                }
            }
            // For external links or links not handled, default behavior will apply if e.preventDefault() wasn't called
        });
    });

    // Close modal listeners
    if (closeTrailerModal) {
        closeTrailerModal.addEventListener('click', () => closeModal(trailerModal, trailerVideo));
    }
    if (closeDemoModal) {
        closeDemoModal.addEventListener('click', () => closeModal(demoModal, demoVideo));
    }

    // Close modal when clicking outside
    window.addEventListener('click', (event) => {
        if (event.target === trailerModal) {
            closeModal(trailerModal, trailerVideo);
        }
        if (event.target === demoModal) {
            closeModal(demoModal, demoVideo);
        }
    });

    // --- Debounced Scroll Handler ---
    const handleScroll = debounce(() => {
        const nav = document.querySelector('.sticky-nav');
        if (!nav) return; // Guard clause

        let scrollTop = window.pageYOffset || document.documentElement.scrollTop;
        if (scrollTop > 50) {
            // Check if style needs changing to avoid unnecessary DOM manipulation
            if (nav.style.backgroundColor !== 'rgba(26, 26, 26, 0.95)') {
                nav.style.backgroundColor = 'rgba(26, 26, 26, 0.95)';
            }
        } else {
            if (nav.style.backgroundColor !== 'rgba(26, 26, 26, 0.9)') {
                nav.style.backgroundColor = 'rgba(26, 26, 26, 0.9)';
            }
        }

        // Close mobile nav if open when scrolling starts
        if (navLinksContainer && burgerButton && navLinksContainer.classList.contains('nav-active')) {
            navLinksContainer.classList.remove('nav-active');
            burgerButton.classList.remove('active');
        }
    }, 15); // Debounce time in ms (adjust as needed)

    // Attach debounced scroll listener
    window.addEventListener('scroll', handleScroll);

});
