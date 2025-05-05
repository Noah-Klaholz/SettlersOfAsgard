document.addEventListener('DOMContentLoaded', () => {

    AOS.init({
        duration: 800,
        offset: 100,
        once: true,
        easing: 'ease-in-out',
    });

    const navLinksContainer = document.getElementById('nav-links');
    const burgerButton = document.getElementById('burger-menu');
    const navLinks = document.querySelectorAll('#nav-links li a'); // Select links inside the ul

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

    // Close nav menu when a link is clicked (for mobile view)
    navLinks.forEach(link => {
        link.addEventListener('click', () => {
            if (navLinksContainer.classList.contains('nav-active')) {
                navLinksContainer.classList.remove('nav-active');
                burgerButton.classList.remove('active'); // Remove burger animation class
            }

            // Smooth scroll logic (already exists, slightly adjusted context)
            const targetId = link.getAttribute('href');
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
        });
    });

    // Prevent default anchor jump for all nav links (moved out of smooth scroll loop)
    document.querySelectorAll('header nav ul li a').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            // Scroll logic is handled within the link click listener above
        });
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
