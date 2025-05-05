document.addEventListener('DOMContentLoaded', () => {

    AOS.init({
        duration: 800,
        offset: 100,
        once: true,
        easing: 'ease-in-out',
    });

    // Smooth scrolling for navigation links
    const navLinks = document.querySelectorAll('header nav ul li a');

    navLinks.forEach(link => {
        link.addEventListener('click', function (e) {
            e.preventDefault();
            const targetId = this.getAttribute('href');
            const targetElement = document.querySelector(targetId);

            if (targetElement) {
                // Calculate position considering sticky nav height
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

    // Change nav background on scroll
    const nav = document.querySelector('.sticky-nav');
    window.addEventListener('scroll', () => {
        let scrollTop = window.pageYOffset || document.documentElement.scrollTop;
        if (scrollTop > 50) {
            nav.style.backgroundColor = 'rgba(26, 26, 26, 0.95)'; // Slightly more opaque
        } else {
            nav.style.backgroundColor = 'rgba(26, 26, 26, 0.9)'; // Default transparency
        }
    });

});
