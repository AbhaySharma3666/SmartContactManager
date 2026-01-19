console.log("Contacts.js");
// const baseURL = "http://localhost:8080";
const baseURL = window.location.origin;
// const baseURL = "https://www.scm20.site";
const viewContactModal = document.getElementById("view_contact_modal");

// options with default values
const options = {
  placement: "bottom-right",
  backdrop: "dynamic",
  backdropClasses: "bg-gray-900/50 dark:bg-gray-900/80 fixed inset-0 z-40",
  closable: true,
  onHide: () => {
    console.log("modal is hidden");
  },
  onShow: () => {
    setTimeout(() => {
      contactModal.classList.add("scale-100");
    }, 50);
  },
  onToggle: () => {
    console.log("modal has been toggled");
  },
};

// instance options object
const instanceOptions = {
  id: "view_contact_modal",
  override: true,
};

const contactModal = new Modal(viewContactModal, options, instanceOptions);

function openContactModal() {
  contactModal.show();
}

function closeContactModal() {
  contactModal.hide();
}

async function loadContactdata(id) {
  console.log(id);
  try {
    const data = await (await fetch(`${baseURL}/api/contacts/${id}`)).json();
    console.log(data);
    
    document.querySelector("#contact_name").innerHTML = data.name;
    document.querySelector("#contact_email").innerHTML = data.email;
    document.querySelector("#contact_image").src = data.picture;
    document.querySelector("#contact_address").innerHTML = data.address || 'Not provided';
    document.querySelector("#contact_phone").innerHTML = data.phoneNumber;
    document.querySelector("#contact_about").innerHTML = data.description || 'No description available';
    
    // Handle website link
    const websiteContainer = document.querySelector("#website_container");
    const websiteLink = document.querySelector("#contact_website");
    if (data.websiteLink && data.websiteLink.trim() !== '') {
      websiteLink.href = data.websiteLink;
      websiteLink.innerHTML = data.websiteLink;
      websiteContainer.style.display = 'flex';
    } else {
      websiteContainer.style.display = 'none';
    }
    
    // Handle LinkedIn link
    const linkedinContainer = document.querySelector("#linkedin_container");
    const linkedinLink = document.querySelector("#contact_linkedIn");
    if (data.linkedInLink && data.linkedInLink.trim() !== '') {
      linkedinLink.href = data.linkedInLink;
      linkedinLink.innerHTML = data.linkedInLink;
      linkedinContainer.style.display = 'flex';
    } else {
      linkedinContainer.style.display = 'none';
    }
    
    // Set action buttons
    document.querySelector("#call_button").href = `tel:${data.phoneNumber}`;
    document.querySelector("#email_button").href = `mailto:${data.email}`;
    
    openContactModal();
  } catch (error) {
    console.log("Error: ", error);
  }
}

// delete contact

async function deleteContact(id) {
  Swal.fire({
    title: "Do you want to delete the contact?",
    icon: "warning",
    showCancelButton: true,
    confirmButtonText: "Delete",
  }).then((result) => {
    /* Read more about isConfirmed, isDenied below */
    if (result.isConfirmed) {
      const url = `${baseURL}/user/contacts/delete/` + id;
      window.location.replace(url);
    }
  });
}

// toggle favorite
async function toggleFavorite(id, event) {
  event.stopPropagation();
  const icon = event.currentTarget.querySelector('i');
  try {
    const data = await (await fetch(`${baseURL}/api/contacts/${id}/toggle-favorite`)).json();
    if (data.favorite) {
      icon.className = 'fa-solid fa-star text-yellow-500 dark:text-yellow-400';
    } else {
      icon.className = 'fa-regular fa-star text-gray-700 dark:text-gray-300';
    }
  } catch (error) {
    console.log("Error: ", error);
  }
}
