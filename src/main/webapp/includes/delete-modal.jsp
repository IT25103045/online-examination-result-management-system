<div class="modal fade" id="deleteModal" tabindex="-1" aria-labelledby="deleteModalTitle" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow-lg">

            <form id="deleteForm" method="post" action="#">
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="recordId" id="deleteRecordId">
                <input type="hidden" name="examId" id="deleteExamId">

                <div class="modal-header">
                    <div>
                        <h5 class="modal-title fw-bold" id="deleteModalTitle">
                            Confirm Delete
                        </h5>
                        <small class="text-secondary">
                            Please confirm before removing this record.
                        </small>
                    </div>

                    <button type="button"
                            class="btn-close"
                            data-bs-dismiss="modal"
                            aria-label="Close"></button>
                </div>

                <div class="modal-body">
                    <div class="d-flex gap-3 align-items-start">
                        <div class="delete-modal-icon">
                            <i class="bi bi-trash3-fill"></i>
                        </div>

                        <div>
                            <p class="mb-1 fw-semibold">
                                This action cannot be undone.
                            </p>

                            <p class="text-secondary mb-0">
                                Are you sure you want to delete
                                <strong id="deleteRecordName">this record</strong>?
                            </p>

                            <div class="alert alert-danger mt-3 mb-0 py-2">
                                <small>
                                    Deleting this record may affect related exam, student, result, or user data.
                                </small>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="modal-footer">
                    <button class="btn btn-outline-secondary"
                            type="button"
                            data-bs-dismiss="modal">
                        Cancel
                    </button>

                    <button class="btn btn-danger"
                            type="submit">
                        <i class="bi bi-trash3 me-1"></i>
                        Delete Record
                    </button>
                </div>
            </form>

        </div>
    </div>
</div>