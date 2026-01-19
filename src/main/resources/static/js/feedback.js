// Feedback page JavaScript

let selectedRating = 0;

// Star rating functionality
document.addEventListener('DOMContentLoaded', function() {
  const stars = document.querySelectorAll('#ratingStars i');
  const ratingInput = document.getElementById('ratingValue');

  stars.forEach(star => {
    star.addEventListener('click', function() {
      selectedRating = parseInt(this.getAttribute('data-rating'));
      ratingInput.value = selectedRating;
      updateStars(selectedRating);
    });

    star.addEventListener('mouseenter', function() {
      const rating = parseInt(this.getAttribute('data-rating'));
      updateStars(rating);
    });
  });

  document.getElementById('ratingStars').addEventListener('mouseleave', function() {
    updateStars(selectedRating);
  });

  function updateStars(rating) {
    stars.forEach((star, index) => {
      if (index < rating) {
        star.classList.remove('fa-regular', 'text-gray-400');
        star.classList.add('fa-solid', 'text-yellow-500');
      } else {
        star.classList.remove('fa-solid', 'text-yellow-500');
        star.classList.add('fa-regular', 'text-gray-400');
      }
    });
  }
});

// Form validation
document.querySelector('form')?.addEventListener('submit', function(e) {
  if (selectedRating === 0) {
    e.preventDefault();
    Swal.fire({
      icon: 'warning',
      title: 'Rating Required',
      text: 'Please select a rating before submitting',
      theme: 'auto'
    });
  }
});

console.log("Feedback page loaded");
