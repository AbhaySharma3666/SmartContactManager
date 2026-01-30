// Update contact form handler

async function validateFileSize(input) {
  if (!input.files[0]) return;
  
  const file = input.files[0];
  const maxSize = 1 * 1024 * 1024; // 1MB
  
  if (file.size > maxSize) {
    const compressed = await compressImage(file);
    const dt = new DataTransfer();
    dt.items.add(compressed);
    input.files = dt.files;
  }
  
  const reader = new FileReader();
  reader.onload = (e) => document.getElementById('upload_image_preview').src = e.target.result;
  reader.readAsDataURL(input.files[0]);
}

function compressImage(file) {
  return new Promise((resolve) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      const img = new Image();
      img.onload = () => {
        const canvas = document.createElement('canvas');
        let width = img.width;
        let height = img.height;
        const maxDim = 1200;
        
        if (width > height && width > maxDim) {
          height *= maxDim / width;
          width = maxDim;
        } else if (height > maxDim) {
          width *= maxDim / height;
          height = maxDim;
        }
        
        canvas.width = width;
        canvas.height = height;
        const ctx = canvas.getContext('2d');
        ctx.drawImage(img, 0, 0, width, height);
        
        canvas.toBlob((blob) => {
          resolve(new File([blob], file.name, { type: 'image/jpeg' }));
        }, 'image/jpeg', 0.7);
      };
      img.src = e.target.result;
    };
    reader.readAsDataURL(file);
  });
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
