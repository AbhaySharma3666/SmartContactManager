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

// Form submission
document.querySelector('form')?.addEventListener('submit', function(e) {
  e.preventDefault();
  
  if (selectedRating === 0) {
    Swal.fire({
      icon: 'warning',
      title: 'Rating Required',
      text: 'Please select a rating before submitting',
      theme: 'auto'
    });
    return;
  }

  const formData = new FormData(this);
  
  fetch('/user/feedback', {
    method: 'POST',
    body: new URLSearchParams(formData)
  })
  .then(res => res.json())
  .then(data => {
    if (data.success) {
      Swal.fire({
        icon: 'success',
        title: 'Success!',
        text: data.message,
        timer: 3000,
        position: 'top-end',
        toast: true,
        showConfirmButton: false,
        theme: 'auto'
      });
      document.querySelector('form').reset();
      selectedRating = 0;
      updateStars(0);
    } else {
      Swal.fire({
        icon: 'error',
        title: 'Error',
        text: data.message,
        theme: 'auto'
      });
    }
  })
  .catch(error => {
    Swal.fire({
      icon: 'error',
      title: 'Error',
      text: 'Failed to submit feedback. Please try again.',
      theme: 'auto'
    });
    console.error('Error:', error);
  });
});

function updateStars(rating) {
  const stars = document.querySelectorAll('#ratingStars i');
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

console.log("Feedback page loaded");
