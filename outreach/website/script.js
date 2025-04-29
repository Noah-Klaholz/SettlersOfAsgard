document.addEventListener('DOMContentLoaded', () => {

    // Initialize AOS (Animate On Scroll library)
    AOS.init({
        duration: 800, // Animation duration in ms
        offset: 100, // Offset (in px) from the original trigger point
        once: true, // Whether animation should happen only once - while scrolling down
        easing: 'ease-in-out', // Default easing for AOS animations
    });

    // Smooth scrolling for navigation links
    const navLinks = document.querySelectorAll('header nav ul li a');

    navLinks.forEach(link => {
        link.addEventListener('click', function (e) {
            e.preventDefault(); // Prevent default anchor jump
            const targetId = this.getAttribute('href'); // Get target section id (e.g., '#features')
            const targetElement = document.querySelector(targetId);

            if (targetElement) {
                // Calculate position to scroll to (considering sticky nav height if necessary)
                const headerOffset = document.querySelector('.sticky-nav').offsetHeight;
                const elementPosition = targetElement.getBoundingClientRect().top;
                const offsetPosition = elementPosition + window.pageYOffset - headerOffset;

                window.scrollTo({
                    top: offsetPosition,
                    behavior: "smooth" // Smooth scroll animation
                });
            }
        });
    });

    // Optional: Change nav background on scroll
    const nav = document.querySelector('.sticky-nav');
    let lastScrollTop = 0;
    window.addEventListener('scroll', () => {
        let scrollTop = window.pageYOffset || document.documentElement.scrollTop;
        if (scrollTop > 50) {
            // Make slightly more opaque on scroll down
            nav.style.backgroundColor = 'rgba(26, 26, 26, 0.95)';
        } else {
            // Return to default transparency at top
            nav.style.backgroundColor = 'rgba(26, 26, 26, 0.9)';
        }
        lastScrollTop = scrollTop <= 0 ? 0 : scrollTop; // For Mobile or negative scrolling
    });

});
