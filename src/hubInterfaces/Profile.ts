/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

import HubInterfaces, { HubInterfacesOptions, HubInterface } from './HubInterfaces';

/**
* A Class that does CRUD operations for storing items as Collections in the Hub
*/
export default class Profile extends HubInterfaces {

  constructor (hubMethodOptions: HubInterfacesOptions) {
    hubMethodOptions.hubInterface = HubInterface.Profile;
    super(hubMethodOptions);
  }
}