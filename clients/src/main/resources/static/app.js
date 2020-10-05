"use strict";

// Define your backend here.
angular.module('demoAppModule', ['ui.bootstrap']).controller('DemoAppCtrl', function($http, $location, $uibModal) {
    const demoApp = this;

    const apiBaseURL = "/api/preAuth/";

    // Retrieves the identity of this and other nodes.
    let peers = [];
    $http.get(apiBaseURL + "me").then((response) => demoApp.thisNode = response.data.me);
    $http.get(apiBaseURL + "peers").then((response) => peers = response.data.peers);

    /** Displays the IOU creation modal. */
        demoApp.openCreatePreAuthModal = () => {
            const createPreAuthModal = $uibModal.open({
                templateUrl: 'createPreAuthModal.html',
                controller: 'CreatePreAuthModalCtrl',
                controllerAs: 'createPreAuthModal',
                resolve: {
                    apiBaseURL: () => apiBaseURL,
                    peers: () => peers
                }
            });

            // Ignores the modal result events.
            createPreAuthModal.result.then(() => {}, () => {});
        };


        demoApp.openApprovalModal = () => {
                    const approvePreAuthModal = $uibModal.open({
                        templateUrl: 'approvePreAuthModal.html',
                        controller: 'ApprovePreAuthModalCtrl',
                        controllerAs: 'approvePreAuthModal',
                        resolve: {
                            apiBaseURL: () => apiBaseURL
                        }
                    });

                    // Ignores the modal result events.
                    approvePreAuthModal.result.then(() => {}, () => {});
                };

        /*
        demoApp.openApprovalModal = () => {
                    const approvePreAuthModal = $uibModal.open({
                    templateUrl: 'approvePreAuthModal.html',
                    controller: 'ApprovePreAuthModalCtrl',
                    controllerAs: 'approvePreAuthModal',
                    resolve: {
                        apiBaseURL: () => apiBaseURL
                        }
                    });

                    approvePreAuthModal.result.then(() => {}, () => {});

                };*/



        /** Refreshes the front-end. */
            demoApp.refresh = () => {
                // Update the list of IOUs.
                $http.get(apiBaseURL + "preAuths").then((response) => demoApp.preAuths =
                    Object.keys(response.data).map((key) => response.data[key].state.data));
            }

            demoApp.refresh();

});

// Causes the webapp to ignore unhandled modal dismissals.
angular.module('demoAppModule').config(['$qProvider', function($qProvider) {
    $qProvider.errorOnUnhandledRejections(false);
}]);