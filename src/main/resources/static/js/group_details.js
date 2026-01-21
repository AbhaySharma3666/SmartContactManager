// Get group data from Thymeleaf inline script
let groupId = '';
let existingMemberIds = [];

// Wait for DOM to load
document.addEventListener('DOMContentLoaded', function() {
  // Get groupId from hidden input if available
  const groupIdInput = document.getElementById('groupId');
  if (groupIdInput) {
    groupId = groupIdInput.value;
  }
});

// Event delegation for add and remove member buttons
document.addEventListener('click', function(e) {
  // Handle member card click (for viewing contact details)
  const memberCard = e.target.closest('.member-card');
  if (memberCard && !e.target.closest('.remove-member-btn')) {
    const contactId = memberCard.dataset.contactId;
    loadContactdata(contactId);
    return;
  }
  
  // Handle remove member button
  const removeBtn = e.target.closest('.remove-member-btn');
  if (removeBtn) {
    e.stopPropagation();
    const contactId = removeBtn.dataset.contactId;
    removeMember(contactId);
    return;
  }
  
  // Handle add member button
  const addBtn = e.target.closest('.add-member-btn');
  if (addBtn) {
    const contactId = addBtn.dataset.contactId;
    addMember(contactId);
  }
});

// Search members
document.getElementById('searchInput')?.addEventListener('input', function(e) {
  const searchTerm = e.target.value.toLowerCase();
  document.querySelectorAll('.member-card').forEach(card => {
    const text = card.textContent.toLowerCase();
    card.style.display = text.includes(searchTerm) ? '' : 'none';
  });
});

// Search contacts in modal
document.getElementById('contactSearch')?.addEventListener('input', function(e) {
  const searchTerm = e.target.value.toLowerCase();
  document.querySelectorAll('.contact-item').forEach(item => {
    const name = item.dataset.name?.toLowerCase() || '';
    const email = item.dataset.email?.toLowerCase() || '';
    item.style.display = (name.includes(searchTerm) || email.includes(searchTerm)) ? '' : 'none';
  });
});

// Filter out existing members from available contacts
setTimeout(() => {
  document.querySelectorAll('.contact-item').forEach(item => {
    if(existingMemberIds.includes(item.dataset.id)){
      item.style.display = 'none';
    }
  });
}, 100);

// Edit modal
function openEditModal() {
  document.getElementById('editModal').classList.remove('hidden');
}

function closeEditModal() {
  document.getElementById('editModal').classList.add('hidden');
}

// Add member modal
function openAddMemberModal() {
  document.getElementById('addMemberModal').classList.remove('hidden');
}

function closeAddMemberModal() {
  document.getElementById('addMemberModal').classList.add('hidden');
}

// Remove member
async function removeMember(contactId) {
  const result = await Swal.fire({
    title: 'Remove Member?',
    text: 'Remove this contact from the group?',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: 'Remove',
    confirmButtonColor: '#dc2626'
  });

  if (result.isConfirmed) {
    try {
      const formData = new URLSearchParams();
      formData.append('contactId', contactId);

      const response = await fetch(`/user/groups/${groupId}/remove-member`, {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: formData
      });
      const data = await response.json();

      if (data.success) {
        Swal.fire({
          icon: 'success',
          title: 'Removed!',
          text: data.message,
          timer: 2000,
          position: 'top-end',
          toast: true,
          showConfirmButton: false
        });
        setTimeout(() => location.reload(), 2000);
      } else {
        Swal.fire({ icon: 'error', title: 'Error', text: data.message });
      }
    } catch (error) {
      Swal.fire({ icon: 'error', title: 'Error', text: 'Failed to remove member' });
    }
  }
}

// Add member
async function addMember(contactId) {
  try {
    const formData = new URLSearchParams();
    formData.append('contactId', contactId);

    const response = await fetch(`/user/groups/${groupId}/add-member`, {
      method: 'POST',
      headers: {'Content-Type': 'application/x-www-form-urlencoded'},
      body: formData
    });
    const data = await response.json();

    if (data.success) {
      Swal.fire({
        icon: 'success',
        title: 'Added!',
        text: data.message,
        timer: 1500,
        position: 'top-end',
        toast: true,
        showConfirmButton: false
      });
      // Hide the added contact from the list
      const contactItem = document.querySelector(`.contact-item[data-id="${contactId}"]`);
      if(contactItem) contactItem.style.display = 'none';
      // Update member count
      const countEl = document.getElementById('memberCount');
      if(countEl) countEl.textContent = data.memberCount;
    } else {
      Swal.fire({ icon: 'error', title: 'Error', text: data.message });
    }
  } catch (error) {
    Swal.fire({ icon: 'error', title: 'Error', text: 'Failed to add member' });
  }
}

// Edit form
document.getElementById('editForm')?.addEventListener('submit', async function(e) {
  e.preventDefault();
  const name = document.getElementById('editGroupName').value;
  const description = document.getElementById('editGroupDescription').value;

  if (!name.trim()) {
    Swal.fire({ icon: 'error', title: 'Error', text: 'Please enter a group name' });
    return;
  }

  try {
    const formData = new URLSearchParams();
    formData.append('name', name);
    formData.append('description', description);

    const response = await fetch(`/user/groups/update/${groupId}`, {
      method: 'POST',
      headers: {'Content-Type': 'application/x-www-form-urlencoded'},
      body: formData
    });
    const data = await response.json();

    if (data.success) {
      closeEditModal();
      Swal.fire({
        icon: 'success',
        title: 'Updated!',
        text: data.message,
        timer: 2000,
        position: 'top-end',
        toast: true,
        showConfirmButton: false
      }).then(() => location.reload());
    } else {
      Swal.fire({ icon: 'error', title: 'Error', text: data.message });
    }
  } catch (error) {
    Swal.fire({ icon: 'error', title: 'Error', text: 'Failed to update group' });
  }
});
