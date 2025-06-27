// Advanced user behavior tracking
const trackUserJourney = () => {
    let startTime = Date.now();
    let sectionTimes = {};

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const sectionId = entry.target.id;
                sectionTimes[sectionId] = Date.now() - startTime;
                // Send to analytics
            }
        });
    });

    document.querySelectorAll('section[id]').forEach(section => {
        observer.observe(section);
    });
};

// A/B testing framework
const runABTest = (testName, variants) => {
    const userId = localStorage.getItem('userId') || Math.random().toString(36);
    localStorage.setItem('userId', userId);

    const variant = variants[Math.floor(Math.random() * variants.length)];
    document.body.setAttribute('data-variant', variant);
    return variant;
};
