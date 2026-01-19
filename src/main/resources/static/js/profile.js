// Profile page JavaScript

let originalPhone = '';

function openEditModal() {
  document.getElementById('editModal').classList.remove('hidden');
}

function closeEditModal() {
  document.getElementById('editModal').classList.add('hidden');
}

function previewFile(input) {
  if (input.files && input.files[0]) {
    const reader = new FileReader();
    reader.onload = function(e) {
      document.getElementById('previewImage').src = e.target.result;
    }
    reader.readAsDataURL(input.files[0]);
  }
}

function sendOTP() {
  const phone = document.getElementById('phoneInput').value.trim();
  
  if (!phone) {
    Swal.fire({
      icon: 'error',
      title: 'Error',
      text: 'Please enter phone number',
      theme: 'auto'
    });
    return;
  }

  if (!phone.startsWith('+')) {
    Swal.fire({
      icon: 'error',
      title: 'Error',
      text: 'Phone number must start with + and country code (e.g., +919876543210)',
      theme: 'auto'
    });
    return;
  }

  Swal.fire({
    title: 'Sending OTP...',
    text: 'Please wait',
    allowOutsideClick: false,
    theme: 'auto',
    didOpen: () => {
      Swal.showLoading();
    }
  });

  fetch('/user/profile/send-otp', {
    method: 'POST',
    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
    body: 'phoneNumber=' + encodeURIComponent(phone)
  })
  .then(res => res.json())
  .then(data => {
    Swal.close();
    if (data.success) {
      document.getElementById('otpSection').classList.remove('hidden');
      Swal.fire({
        icon: 'success',
        title: 'OTP Sent!',
        text: data.message,
        timer: 3000,
        position: 'top-end',
        toast: true,
        showConfirmButton: false,
        theme: 'auto'
      });
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
    Swal.close();
    Swal.fire({
      icon: 'error',
      title: 'Error',
      text: 'Failed to send OTP. Please try again.',
      theme: 'auto'
    });
    console.error('Error:', error);
  });
}

function verifyOTP() {
  const phone = document.getElementById('phoneInput').value;
  const otp = document.getElementById('otpInput').value;

  if (!otp) {
    Swal.fire({
      icon: 'error',
      title: 'Error',
      text: 'Please enter OTP',
      theme: 'auto'
    });
    return;
  }

  fetch('/user/profile/verify-otp', {
    method: 'POST',
    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
    body: 'phoneNumber=' + encodeURIComponent(phone) + '&otp=' + encodeURIComponent(otp)
  })
  .then(res => res.json())
  .then(data => {
    if (data.success) {
      Swal.fire({
        icon: 'success',
        title: 'Success',
        text: data.message,
        theme: 'auto'
      }).then(() => location.reload());
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
      text: 'Failed to verify OTP. Please try again.',
      theme: 'auto'
    });
    console.error('Error:', error);
  });
}

console.log("Profile page loaded");
