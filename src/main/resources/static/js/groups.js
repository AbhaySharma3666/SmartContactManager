// View group details
function viewGroup(groupId) {
  window.location.href = '/user/groups/details/' + groupId;
}

// Modal functions
function openCreateModal() {
  document.getElementById('createModal').classList.remove('hidden');
}

function closeCreateModal() {
  document.getElementById('createModal').classList.add('hidden');
  document.getElementById('groupForm').reset();
}

// Delete group
async function deleteGroup(groupId) {
  const result = await Swal.fire({
    title: 'Delete Group?',
    text: 'This action cannot be undone',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: 'Delete',
    confirmButtonColor: '#dc2626'
  });

  if (result.isConfirmed) {
    try {
      const response = await fetch(`/user/groups/delete/${groupId}`, {
        method: 'POST'
      });
      const data = await response.json();

      if (data.success) {
        Swal.fire({
          icon: 'success',
          title: 'Deleted!',
          text: data.message,
          timer: 2000,
          position: 'top-end',
          toast: true,
          showConfirmButton: false
        }).then(() => {
          location.reload();
        });
      } else {
        Swal.fire({ icon: 'error', title: 'Error', text: data.message });
      }
    } catch (error) {
      Swal.fire({ icon: 'error', title: 'Error', text: 'Failed to delete group' });
    }
  }
}

// Event delegation for group cards and delete buttons
document.addEventListener('click', function(e) {
  // Handle group card click
  const groupCard = e.target.closest('.group-card');
  if (groupCard && !e.target.closest('.delete-group-btn')) {
    const groupId = groupCard.dataset.groupId;
    viewGroup(groupId);
  }
  
  // Handle delete button click
  const deleteBtn = e.target.closest('.delete-group-btn');
  if (deleteBtn) {
    e.stopPropagation();
    const groupId = deleteBtn.dataset.groupId;
    deleteGroup(groupId);
  }
});

// Handle form submission
document.addEventListener('DOMContentLoaded', function() {
  const groupForm = document.getElementById('groupForm');
  if (groupForm) {
    groupForm.addEventListener('submit', async function(e) {
      e.preventDefault();
      const name = document.getElementById('groupName').value;
      const description = document.getElementById('groupDescription').value;

      if (!name.trim()) {
        Swal.fire({ icon: 'error', title: 'Error', text: 'Please enter a group name' });
        return;
      }

      try {
        const formData = new URLSearchParams();
        formData.append('name', name);
        formData.append('description', description || '');

        const response = await fetch('/user/groups/create', {
          method: 'POST',
          headers: {'Content-Type': 'application/x-www-form-urlencoded'},
          body: formData
        });
        const data = await response.json();

        if (data.success) {
          closeCreateModal();
          Swal.fire({
            icon: 'success',
            title: 'Success!',
            text: data.message,
            timer: 2000,
            position: 'top-end',
            toast: true,
            showConfirmButton: false
          }).then(() => {
            location.reload();
          });
        } else {
          Swal.fire({ icon: 'error', title: 'Error', text: data.message });
        }
      } catch (error) {
        Swal.fire({ icon: 'error', title: 'Error', text: 'Failed to create group' });
      }
    });
  }
});
