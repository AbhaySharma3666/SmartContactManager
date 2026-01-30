// Update contact form handler

function validateFileSize(input) {
  const maxSize = 2 * 1024 * 1024; // 2MB in bytes
  if (input.files[0] && input.files[0].size > maxSize) {
    alert('File size must be less than 2MB. Please select a smaller image.');
    input.value = '';
    const preview = document.getElementById('upload_image_preview');
    if (preview) preview.src = preview.getAttribute('data-th-src') || '';
  }
}

document.addEventListener('DOMContentLoaded', function() {
  const updateForm = document.querySelector('form[action*="/user/contacts/update/"]');
  
  if (updateForm) {
    updateForm.addEventListener('submit', async function(e) {
      e.preventDefault();
      
      const formData = new FormData(this);
      const actionUrl = this.action;
      
      try {
        const response = await fetch(actionUrl, {
          method: 'POST',
          body: formData
        });
        
        if (response.redirected || response.ok) {
          Swal.fire({
            icon: 'success',
            title: 'Success!',
            text: 'Contact Updated Successfully!',
            position: 'top-end',
            toast: true,
            timer: 1000,
            showConfirmButton: false,
            timerProgressBar: true
          }).then(() => {
            window.location.href = '/user/contacts';
          });
        } else {
          Swal.fire({
            icon: 'error',
            title: 'Error!',
            text: 'Failed to update contact',
            position: 'top-end',
            toast: true,
            timer: 1000,
            showConfirmButton: false
          });
        }
      } catch (error) {
        console.error('Error:', error);
        Swal.fire({
          icon: 'error',
          title: 'Error!',
          text: 'An error occurred while updating contact',
          position: 'top-end',
          toast: true,
          timer: 2000,
          showConfirmButton: false
        });
      }
    });
  }
});
