/**
 * Copyright  2006-2010 Soyatec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id$
 */
package org.soyatec.windowsazure.blob;

import java.util.List;

/**
 * This interface represents a blob that is made out of blocks and provides APIs to directly access it.
 * Block blobs are comprised of blocks, each of which is identified by a block ID. You create or modify a block blob by uploading a set of blocks and committing them by their block IDs.
 * See http://msdn.microsoft.com/en-us/library/ee691964.aspx for more detail.
 *
 */
public interface IBlockBlob extends IBlob {

	/**
	 * @return The size of the block.
	 */
	public long getBlockSize();

	/**
	 * @param blockSize
	 *            The size of the block.
	 */
	public void setBlockSize(long blockSize);

	/**
	 * Writes a blob by specifying the list of block IDs that make up the blob.
	 *
	 * @param blockList
	 *            A list of block ID
	 */
	public void putBlockList(List<String> blockList);

	/**
	 * The Put Block operation creates a new block to be committed as part of a
	 * blob.
	 *
	 * @param blockId
	 *            A valid Base64 string value that identifies the block. Prior
	 *            to encoding, the string must be less than or equal to 64 bytes
	 *            in size. For a given blob, the length of the value specified
	 *            for the blockid parameter must be the same size for each
	 *            block. Note that the Base64 string must be URL-encoded.
	 *
	 * @param contents
	 * 			The content that will be written to specified block
	 */
	public void putBlock(String blockId, IBlobContents contents);

}
