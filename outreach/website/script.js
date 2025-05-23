document.addEventListener('DOMContentLoaded', () => {
    AOS.init({
        duration: 800,
        offset: 100,
        once: true,
        easing: 'ease-in-out',
    });

    const $ = (id) => document.getElementById(id);
    const $$ = (selector) => document.querySelector(selector);

    const elements = {
        navLinksContainer: $('nav-links'),
        burgerButton: $('burger-menu'),
        stickyNav: $$('.sticky-nav'),
        trailerModal: $('trailer-modal'),
        demoModal: $('demo-modal'),
        trailerVideo: $('trailer-video'),
        demoVideo: $('demo-video')
    };

    const { navLinksContainer, burgerButton, stickyNav, trailerModal, demoModal, trailerVideo, demoVideo } = elements;

    const debounce = (func, wait = 15) => {
        let timeout;
        return (...args) => {
            clearTimeout(timeout);
            timeout = setTimeout(() => func.apply(this, args), wait);
        };
    };

    const closeNavMenu = () => {
        if (navLinksContainer?.classList.contains('nav-active')) {
            navLinksContainer.classList.remove('nav-active');
            burgerButton?.classList.remove('active');
        }
    };

    const openModal = (modal, video) => {
        video.style.display = 'block';
        video.play().catch(() => { });
        modal.classList.add('show');
        closeNavMenu();
    };

    const closeModal = (modal, video) => {
        modal.classList.remove('show');
        video.pause();
        video.currentTime = 0;
    };

    const scrollToElement = (href) => {
        const targetElement = $$(href);
        if (targetElement && stickyNav) {
            const headerOffset = stickyNav.offsetHeight || 0;
            const elementPosition = targetElement.getBoundingClientRect().top;
            const offsetPosition = elementPosition + window.pageYOffset - headerOffset;

            window.scrollTo({
                top: offsetPosition,
                behavior: "smooth"
            });
        }
    };

    // Optimized event delegation with early returns
    document.addEventListener('click', (e) => {
        const { target } = e;
        const targetId = target.id;

        // Handle burger menu
        if (target.closest('#burger-menu')) {
            navLinksContainer?.classList.toggle('nav-active');
            burgerButton?.classList.toggle('active');
            return;
        }

        // Handle modal close buttons
        if (targetId === 'close-trailer-modal') {
            e.stopPropagation();
            closeModal(trailerModal, trailerVideo);
            return;
        }

        if (targetId === 'close-demo-modal') {
            e.stopPropagation();
            closeModal(demoModal, demoVideo);
            return;
        }

        // Handle modal background clicks
        if (target === trailerModal) {
            closeModal(trailerModal, trailerVideo);
            return;
        }

        if (target === demoModal) {
            closeModal(demoModal, demoVideo);
            return;
        }

        // Handle navigation and video buttons
        const link = target.closest('a');
        if (!link) return;

        const href = link.getAttribute('href');
        const linkId = link.id;

        switch (linkId) {
            case 'watch-trailer-link':
            case 'world-play-btn':
                e.preventDefault();
                openModal(trailerModal, trailerVideo);
                break;
            case 'watch-demo-link':
            case 'demo-play-btn':
                e.preventDefault();
                openModal(demoModal, demoVideo);
                break;
            default:
                if (href?.startsWith('#')) {
                    e.preventDefault();
                    scrollToElement(href);
                    closeNavMenu();
                }
        }
    });

    // Optimized scroll handler with cached color values
    const scrollColors = {
        scrolled: 'rgba(26, 26, 26, 0.95)',
        default: 'rgba(26, 26, 26, 0.9)'
    };

    const handleScroll = debounce(() => {
        if (!stickyNav) return;

        const scrollTop = window.pageYOffset;
        const newColor = scrollTop > 50 ? scrollColors.scrolled : scrollColors.default;

        if (stickyNav.style.backgroundColor !== newColor) {
            stickyNav.style.backgroundColor = newColor;
        }

        closeNavMenu();
    }, 15);

    window.addEventListener('scroll', handleScroll, { passive: true });
});
