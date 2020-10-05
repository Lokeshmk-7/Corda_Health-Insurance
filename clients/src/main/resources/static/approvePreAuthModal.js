"use strict";

angular.module('demoAppModule').controller('ApprovePreAuthModalCtrl', function($http, $uibModalInstance, $uibModal, apiBaseURL) {

    const approvePreAuthModal = this
    approvePreAuthModal.form = {};

    approvePreAuthModal.create = () => {

        const id = approvePreAuthModal.form.id;

        $uibModalInstance.close();

        const approvePreAuthEndpoint =
        apiBaseURL +
        `approvePreAuthEndpoint?id=${id}`;

        $http.get(approvePreAuthEndpoint).then(
             (result) => approvePreAuthModal.displayMessage(result),
             (result) => approvePreAuthModal.displayMessage(result)
            );

    };


    approvePreAuthModal.displayMessage = (message) => {
        const approvePreAuthMsgModal = $uibModal.open({
            templateUrl: 'approvePreAuthMsgModal.html',
            controller: 'approvePreAuthMsgModalCtrl',
            controllerAs: 'approvePreAuthMsgModal',
            resolve: {
                message: () => message
            }
        });

        approvePreAuthMsgModal.result.then(() => {}, () => {});

    };

});


/*
"use strict";

angular.module('demoAppModule').controller('ApprovePreAuthModalCtrl', function($http, $uibModalInstance, $uibModal, apiBaseURL) {
    const approvePreAuthModal = this;

    approvePreAuthModal.form = {};

    //Validate and create an IOU.
    approvePreAuthModal.create = () => {

            const id = approvePreAuthModal.form.id;

            $uibModalInstance.close();

            // We define the IOU creation endpoint.
            const issuePreAuthEndpoint =
                apiBaseURL +
                `issuePreAuthEndpoint?id=${id}`;


            // We hit the endpoint to create the IOU and handle success/failure responses.
            $http.put(issuePreAuthEndpoint).then(
                  (result) => approvePreAuthModal.displayMessage(result),
                  (result) => approvePreAuthModal.displayMessage(result)
                  );

    };

    // Displays the success/failure response from attempting to create an IOU.
    approvePreAuthModal.displayMessage = (message) => {
        const approvePreAuthMsgModal = $uibModal.open({
            templateUrl: 'approvePreAuthMsgModal.html',
            controller: 'approvePreAuthMsgModalCtrl',
            controllerAs: 'approvePreAuthMsgModal',
            resolve: {
                message: () => message
            }
        });

        // No behaviour on close / dismiss.
        approvePreAuthMsgModal.result.then(() => {}, () => {});
    };

    // Closes the IOU creation modal.
    //createPreAuthModal.cancel = () => $uibModalInstance.dismiss();

//});
*/



// Controller for the success/fail modal.
angular.module('demoAppModule').controller('approvePreAuthMsgModalCtrl', function($uibModalInstance, message) {
    const approvePreAuthMsgModal = this;
    approvePreAuthMsgModal.message = message.data;
});