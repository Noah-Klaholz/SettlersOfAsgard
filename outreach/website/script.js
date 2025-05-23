document.addEventListener('DOMContentLoaded', () => {
    AOS.init({
        duration: 800,
        offset: 100,
        once: true,
        easing: 'ease-in-out',
    });

    const $ = id => document.getElementById(id);
    const $$ = selector => document.querySelector(selector);

    const elements = {
        navLinksContainer: $('nav-links'),
        burgerButton: $('burger-menu'),
        stickyNav: $$('.sticky-nav'),
        trailerModal: $('trailer-modal'),
        demoModal: $('demo-modal'),
        trailerVideo: $('trailer-video'),
        demoVideo: $('demo-video')
    };

    const debounce = (func, wait = 15) => {
        let timeout;
        return (...args) => {
            clearTimeout(timeout);
            timeout = setTimeout(() => func.apply(this, args), wait);
        };
    };

    const closeNavMenu = () => {
        const { navLinksContainer, burgerButton } = elements;
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
        if (targetElement && elements.stickyNav) {
            const headerOffset = elements.stickyNav.offsetHeight || 0;
            const elementPosition = targetElement.getBoundingClientRect().top;
            const offsetPosition = elementPosition + window.pageYOffset - headerOffset;

            window.scrollTo({
                top: offsetPosition,
                behavior: "smooth"
            });
        }
    };

    // Event delegation for navigation clicks
    document.addEventListener('click', (e) => {
        const target = e.target;
        const link = target.closest('a');
        const button = target.closest('button');

        // Handle burger menu
        if (target.closest('#burger-menu')) {
            elements.navLinksContainer?.classList.toggle('nav-active');
            elements.burgerButton?.classList.toggle('active');
            return;
        }

        // Handle modal close buttons with early return
        const targetId = target.id;
        if (targetId === 'close-trailer-modal') {
            e.stopPropagation();
            closeModal(elements.trailerModal, elements.trailerVideo);
            return;
        }

        if (targetId === 'close-demo-modal') {
            e.stopPropagation();
            closeModal(elements.demoModal, elements.demoVideo);
            return;
        }

        // Handle modal background clicks
        if (e.target === elements.trailerModal) {
            closeModal(elements.trailerModal, elements.trailerVideo);
            return;
        }

        if (e.target === elements.demoModal) {
            closeModal(elements.demoModal, elements.demoVideo);
            return;
        }

        // Handle navigation and video buttons
        if (!link) return;

        const href = link.getAttribute('href');
        const linkId = link.id;

        switch (linkId) {
            case 'watch-trailer-link':
            case 'world-play-btn':
                e.preventDefault();
                openModal(elements.trailerModal, elements.trailerVideo);
                break;
            case 'watch-demo-link':
            case 'demo-play-btn':
                e.preventDefault();
                openModal(elements.demoModal, elements.demoVideo);
                break;
            default:
                if (href?.startsWith('#')) {
                    e.preventDefault();
                    scrollToElement(href);
                    closeNavMenu();
                }
        }
    });

    const handleScroll = debounce(() => {
        const { stickyNav } = elements;
        if (!stickyNav) return;

        const scrollTop = window.pageYOffset;
        const newColor = scrollTop > 50 ? 'rgba(26, 26, 26, 0.95)' : 'rgba(26, 26, 26, 0.9)';

        if (stickyNav.style.backgroundColor !== newColor) {
            stickyNav.style.backgroundColor = newColor;
        }

        closeNavMenu();
    }, 15);

    window.addEventListener('scroll', handleScroll, { passive: true });
});
