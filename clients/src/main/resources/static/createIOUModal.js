"use strict";

angular.module('demoAppModule').controller('CreatePreAuthModalCtrl', function($http, $uibModalInstance, $uibModal, apiBaseURL, peers) {
    const createPreAuthModal = this;

    createPreAuthModal.peers = peers;
    createPreAuthModal.form = {};
    createPreAuthModal.formError = false;

    /** Validate and create an IOU. */
    createPreAuthModal.create = () => {
        if (invalidFormInput()) {
            createPreAuthModal.formError = true;
        } else {
            createPreAuthModal.formError = false;

            const TPA = createPreAuthModal.form.TPA;
            const Insurer = createPreAuthModal.form.Insurer;
            const DoA = createPreAuthModal.form.DoA;
            const EstDoS = createPreAuthModal.form.EstDoS;
            const Diagnosis = createPreAuthModal.form.Diagnosis;
            const Package = createPreAuthModal.form.Package;
            const Doctor = createPreAuthModal.form.Doctor;


            $uibModalInstance.close();

            // We define the IOU creation endpoint.
            const issuePreAuthEndpoint =
                apiBaseURL +
                `issuePreAuthEndpoint?TPA=${TPA}&Insurer=${Insurer}&DoA=${DoA}&EstDoS=${EstDoS}&Diagnosis=${Diagnosis}&Package=${Package}&Doctor=${Doctor}`;


            // We hit the endpoint to create the IOU and handle success/failure responses.
            $http.put(issuePreAuthEndpoint).then(
                  (result) => createPreAuthModal.displayMessage(result),
                  (result) => createPreAuthModal.displayMessage(result)
                  );

            // We hit the endpoint to create the IOU and handle success/failure responses.
             $http.get("/api/preAuth/me").then(
                   (result) => createPreAuthModal.displayMessage(result),
                   (result) => createPreAuthModal.displayMessage(result)
                   );

        }
    };

    /** Displays the success/failure response from attempting to create an IOU. */
    createPreAuthModal.displayMessage = (message) => {
        const createPreAuthMsgModal = $uibModal.open({
            templateUrl: 'createPreAuthMsgModal.html',
            controller: 'createPreAuthMsgModalCtrl',
            controllerAs: 'createPreAuthMsgModal',
            resolve: {
                message: () => message
            }
        });

        // No behaviour on close / dismiss.
        createPreAuthMsgModal.result.then(() => {}, () => {});
    };

    /** Closes the IOU creation modal. */
    createPreAuthModal.cancel = () => $uibModalInstance.dismiss();

    // Validates the IOU.
    function invalidFormInput() {
        return isNaN(createPreAuthModal.form.DoA) || (createPreAuthModal.form.TPA === undefined);
    }
});

// Controller for the success/fail modal.
angular.module('demoAppModule').controller('createPreAuthMsgModalCtrl', function($uibModalInstance, message) {
    const createPreAuthMsgModal = this;
    createPreAuthMsgModal.message = message.data;
});