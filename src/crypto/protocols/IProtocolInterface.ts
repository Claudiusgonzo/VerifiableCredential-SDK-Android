/*---------------------------------------------------------------------------------------------
 *  Copyright (c) Microsoft Corporation. All rights reserved.
 *  Licensed under the MIT License. See License in the project root for license information.
 *--------------------------------------------------------------------------------------------*/
import IProtocolOptions from "./IProtocolOptions";
import { PublicKey } from "../..";
import IVerificationResult from "./IVerificationResult";
import { ICryptoToken } from "./ICryptoToken";

/**
 * Interface defining the implementation of the selected protocol.
 */
export interface IProtocolInterface {

  /**
   * Signs contents using the given private key reference.
   *
   * @param signingKeyReference Reference to the signing key.
   * @param payload to sign.
   * @param format of the final signature.
   * @param options used for the signature. These options override the options provided in the constructor.
   * @returns Signed payload in requested format.
   */
   sign (signingKeyReference: string, payload: Buffer, format: string, options?: IProtocolOptions): Promise<ICryptoToken>;

  /**
   * Verify the signature.
   *
   * @param validationKeys Public key to validate the signature.
   * @param payload that was signed
   * @param signature on payload  
   * @param options used for the signature. These options override the options provided in the constructor.
   * @returns True if signature validated.
   */
   verify (validationKeys: PublicKey[], payload: Buffer, signature: ICryptoToken, options?: IProtocolOptions): Promise<IVerificationResult>;

  /**
   * Encrypt content using the given public keys in JWK format.
   * The key type enforces the key encryption algorithm.
   * The options can override certain algorithm choices.
   * 
   * @param recipients List of recipients' public keys.
   * @param payload to encrypt.
   * @param format of the final serialization.
   * @param options used for the signature. These options override the options provided in the constructor.
   * @returns JweToken with encrypted payload.
   */
   encrypt (recipients: PublicKey[], payload: Buffer, format: string, options?: IProtocolOptions): Promise<ICryptoToken>;

  /**
   * Decrypt the content.
   * 
   * @param decryptionKeyReference Reference to the decryption key.
   * @param options used for the decryption. These options override the options provided in the constructor.
   * @returns Decrypted payload.
   */
   decrypt (decryptionKeyReference: string, cipher: ICryptoToken, options?: IProtocolOptions): Promise<Buffer>;

   /**
   * Serialize a cryptographic token
   * @param format Optional specify the serialization format. If not specified, use default format.
   */
   serialize (format?: any): string;

  /**
   * Deserialize a cryptographic token
   */
  deserialize (token: string, options?: IProtocolOptions): ICryptoToken;
}
