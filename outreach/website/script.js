document.addEventListener('DOMContentLoaded', () => {
    AOS.init({
        duration: 800,
        offset: 100,
        once: true,
        easing: 'ease-in-out',
    });

    const elements = {
        navLinksContainer: document.getElementById('nav-links'),
        burgerButton: document.getElementById('burger-menu'),
        allNavLinks: document.querySelectorAll('#nav-links li a'),
        stickyNav: document.querySelector('.sticky-nav'),
        trailerModal: document.getElementById('trailer-modal'),
        demoModal: document.getElementById('demo-modal'),
        closeTrailerModal: document.getElementById('close-trailer-modal'),
        closeDemoModal: document.getElementById('close-demo-modal'),
        trailerVideo: document.getElementById('trailer-video'),
        demoVideo: document.getElementById('demo-video'),
        worldPlayBtn: document.getElementById('world-play-btn'),
        demoPlayButton: document.getElementById('demo-play-btn')
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
        const targetElement = document.querySelector(href);
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

    // Event listeners setup
    const {
        burgerButton, navLinksContainer, worldPlayBtn, demoPlayButton,
        allNavLinks, closeTrailerModal, closeDemoModal,
        trailerModal, demoModal, trailerVideo, demoVideo
    } = elements;

    if (burgerButton && navLinksContainer) {
        burgerButton.addEventListener('click', () => {
            navLinksContainer.classList.toggle('nav-active');
            burgerButton.classList.toggle('active');
        });
    }

    worldPlayBtn?.addEventListener('click', (e) => {
        e.preventDefault();
        openModal(trailerModal, trailerVideo);
    });

    demoPlayButton?.addEventListener('click', (e) => {
        e.preventDefault();
        openModal(demoModal, demoVideo);
    });

    allNavLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            const href = link.getAttribute('href');
            const linkId = link.id;

            switch (linkId) {
                case 'watch-trailer-link':
                    e.preventDefault();
                    openModal(trailerModal, trailerVideo);
                    break;
                case 'watch-demo-link':
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
    });

    closeTrailerModal?.addEventListener('click', (e) => {
        e.stopPropagation();
        closeModal(trailerModal, trailerVideo);
    });

    closeDemoModal?.addEventListener('click', (e) => {
        e.stopPropagation();
        closeModal(demoModal, demoVideo);
    });

    window.addEventListener('click', (event) => {
        if (event.target === trailerModal) closeModal(trailerModal, trailerVideo);
        if (event.target === demoModal) closeModal(demoModal, demoVideo);
    });

    const handleScroll = debounce(() => {
        const { stickyNav } = elements;
        if (!stickyNav) return;

        const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
        const newColor = scrollTop > 50 ? 'rgba(26, 26, 26, 0.95)' : 'rgba(26, 26, 26, 0.9)';

        if (stickyNav.style.backgroundColor !== newColor) {
            stickyNav.style.backgroundColor = newColor;
        }

        closeNavMenu();
    }, 15);

    window.addEventListener('scroll', handleScroll, { passive: true });
});
