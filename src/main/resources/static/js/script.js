document.addEventListener('DOMContentLoaded', function() {
    // Image preview on file selection
    const fileInput = document.getElementById('file');
    const imagePreview = document.getElementById('imagePreview');
    const previewContainer = document.getElementById('preview');

    if (fileInput) {
        fileInput.addEventListener('change', function() {
            if (this.files && this.files[0]) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    imagePreview.src = e.target.result;
                    previewContainer.classList.remove('d-none');
                };
                reader.readAsDataURL(this.files[0]);
            } else {
                imagePreview.src = '';
                previewContainer.classList.add('d-none');
            }
        });
    }

    // Delete image functionality - using event delegation
    const deleteModal = new bootstrap.Modal(document.getElementById('deleteModal'));
    const confirmDeleteButton = document.getElementById('confirmDelete');
    let imageKeyToDelete = null;

    // Use event delegation for delete buttons
    document.body.addEventListener('click', function(event) {
        const deleteBtn = event.target.closest('.delete-btn');
        if (deleteBtn) {
            imageKeyToDelete = deleteBtn.getAttribute('data-key');
            deleteModal.show();
        }
    });

    if (confirmDeleteButton) {
        confirmDeleteButton.addEventListener('click', function() {
            if (imageKeyToDelete) {
                deleteImage(imageKeyToDelete);
            }
        });
    }

    function deleteImage(key) {
        axios.delete(`/api/images/${encodeURIComponent(key)}`)
            .then(response => {
                if (response.data.status === 'success') {
                    deleteModal.hide();
                    removeImageFromGallery(key);
                    showToast('Image deleted successfully', 'success');
                } else {
                    deleteModal.hide();
                    showToast('Failed to delete image: ' + response.data.message, 'danger');
                }
            })
            .catch(error => {
                deleteModal.hide();
                showToast('An error occurred while deleting the image', 'danger');
                console.error('Error:', error);
            });
    }

    function removeImageFromGallery(key) {
        const imageCard = document.querySelector(`.delete-btn[data-key="${key}"]`).closest('.col-md-4');
        imageCard.style.opacity = '0';
        setTimeout(() => {
            imageCard.remove();
            checkIfGalleryEmpty();
        }, 300);
    }

    function checkIfGalleryEmpty() {
        if (document.querySelectorAll('#imageGallery .col-md-4').length === 0) {
            document.getElementById('imageGallery').innerHTML = `
                <div class="col-12 text-center py-5">
                    <i class="fas fa-images fa-3x text-muted mb-3"></i>
                    <h4 class="text-muted">No images found</h4>
                    <p class="text-muted">Upload your first image to get started!</p>
                </div>
            `;
        }
    }

    // Toast notifications
    function showToast(message, type) {
        const toastContainer = document.createElement('div');
        toastContainer.className = 'toast-container position-fixed bottom-0 end-0 p-3';

        const toast = document.createElement('div');
        toast.className = `toast align-items-center text-white bg-${type} border-0`;
        toast.setAttribute('role', 'alert');
        toast.setAttribute('aria-live', 'assertive');
        toast.setAttribute('aria-atomic', 'true');

        toast.innerHTML = `
            <div class="d-flex">
                <div class="toast-body">
                    ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        `;

        toastContainer.appendChild(toast);
        document.body.appendChild(toastContainer);

        const bsToast = new bootstrap.Toast(toast, { delay: 5000 });
        bsToast.show();

        toast.addEventListener('hidden.bs.toast', function() {
            toastContainer.remove();
        });
    }

    // Form validation
    const uploadForm = document.getElementById('uploadForm');
    if (uploadForm) {
        uploadForm.addEventListener('submit', function(event) {
            if (!fileInput.files || fileInput.files.length === 0) {
                event.preventDefault();
                showToast('Please select a file to upload', 'danger');
            }
        });
    }

    // Auto-close alerts
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }, 5000);
    });
});