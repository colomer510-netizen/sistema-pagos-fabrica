document.addEventListener('DOMContentLoaded', function () {
    var links = document.querySelectorAll('a[data-confirm]');
    links.forEach(function (link) {
        link.addEventListener('click', function (event) {
            var mensaje = link.getAttribute('data-confirm');
            if (!window.confirm(mensaje)) {
                event.preventDefault();
            }
        });
    });
});
