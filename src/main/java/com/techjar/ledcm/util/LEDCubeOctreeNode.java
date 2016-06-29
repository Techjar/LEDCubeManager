
package com.techjar.ledcm.util;

/**
 *
 * @author Techjar
 */
public class LEDCubeOctreeNode {
    private final AxisAlignedBB aabb;
    private final Vector3 ledPosition;
    private LEDCubeOctreeNode[] nodes;

    public LEDCubeOctreeNode(AxisAlignedBB aabb, Vector3 ledPosition) {
        this.aabb = aabb;
        this.ledPosition = ledPosition;
        this.nodes = new LEDCubeOctreeNode[8];
    }

    public LEDCubeOctreeNode(AxisAlignedBB aabb) {
        this(aabb, null);
    }

    public AxisAlignedBB getAABB() {
        return aabb;
    }

    public Vector3 getLEDPosition() {
        return ledPosition;
    }

    public LEDCubeOctreeNode getNode(int index) {
        return nodes[index];
    }

    public void setNode(int index, LEDCubeOctreeNode node) {
        nodes[index] = node;
    }
}
