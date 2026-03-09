// Sidebar toggle (mobile)
document.addEventListener('DOMContentLoaded', function () {
    const hamburger = document.querySelector('.hamburger');
    const sidebar = document.querySelector('.sidebar');

    if (hamburger && sidebar) {
        hamburger.addEventListener('click', function () {
            sidebar.classList.toggle('open');
        });

        // Close sidebar when clicking content area on mobile
        document.querySelector('.content-area').addEventListener('click', function () {
            sidebar.classList.remove('open');
        });
    }

    // Active TOC highlighting on scroll
    const tocLinks = document.querySelectorAll('.toc-rail a');
    if (tocLinks.length > 0) {
        const headings = [];
        tocLinks.forEach(function (link) {
            const id = link.getAttribute('href').slice(1);
            const heading = document.getElementById(id);
            if (heading) headings.push({ el: heading, link: link });
        });

        function updateToc() {
            let current = null;
            for (let i = headings.length - 1; i >= 0; i--) {
                if (headings[i].el.getBoundingClientRect().top <= 100) {
                    current = headings[i];
                    break;
                }
            }
            tocLinks.forEach(function (l) { l.classList.remove('active'); });
            if (current) current.link.classList.add('active');
        }

        window.addEventListener('scroll', updateToc, { passive: true });
        updateToc();
    }
});
