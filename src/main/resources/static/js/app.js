document.addEventListener('DOMContentLoaded', function () {
    var confirmables = document.querySelectorAll('a[data-confirm], button[data-confirm], form[data-confirm]');

    confirmables.forEach(function (elemento) {
        elemento.addEventListener('click', function (event) {
            var mensaje = elemento.getAttribute('data-confirm');
            if (!window.confirm(mensaje)) {
                event.preventDefault();
                event.stopPropagation();
            }
        });
    });
});
