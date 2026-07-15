package com.pkfl.creepyhorse.client;

import java.util.ArrayList;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.loading.json.raw.Bone;
import software.bernie.geckolib.loading.json.raw.Cube;
import software.bernie.geckolib.loading.json.raw.ModelProperties;
import software.bernie.geckolib.loading.json.raw.PolyMesh;
import software.bernie.geckolib.loading.object.BakedModelFactory;
import software.bernie.geckolib.loading.object.BoneStructure;
import software.bernie.geckolib.loading.object.GeometryTree;

public final class PolyMeshBakedModelFactory
implements BakedModelFactory {
    private final BakedModelFactory fallback = BakedModelFactory.DEFAULT_FACTORY;

    public BakedGeoModel constructGeoModel(GeometryTree tree) {
        ArrayList<GeoBone> roots = new ArrayList<GeoBone>();
        for (BoneStructure bone : tree.topLevelBones().values()) {
            roots.add(this.constructBone(bone, tree.properties(), null));
        }
        return new BakedGeoModel(roots, tree.properties());
    }

    public GeoBone constructBone(BoneStructure structure, ModelProperties properties, GeoBone parent) {
        Bone source = structure.self();
        GeoBone baked = new GeoBone(parent, source.name(), source.mirror(), source.inflate(), source.neverRender(), source.reset());
        double[] rotation = source.rotation();
        double[] pivot = source.pivot();
        baked.updateRotation((float)Math.toRadians(-PolyMeshBakedModelFactory.component(rotation, 0)), (float)Math.toRadians(-PolyMeshBakedModelFactory.component(rotation, 1)), (float)Math.toRadians(PolyMeshBakedModelFactory.component(rotation, 2)));
        baked.updatePivot((float)(-PolyMeshBakedModelFactory.component(pivot, 0)), (float)PolyMeshBakedModelFactory.component(pivot, 1), (float)PolyMeshBakedModelFactory.component(pivot, 2));
        if (source.cubes() != null) {
            for (Cube cube : source.cubes()) {
                baked.getCubes().add(this.constructCube(cube, properties, baked));
            }
        }
        if (source.polyMesh() != null) {
            baked.getCubes().add(this.buildPolyMeshCube(source.polyMesh(), properties));
        }
        for (BoneStructure child : structure.children().values()) {
            baked.getChildBones().add(this.constructBone(child, properties, baked));
        }
        return baked;
    }

    public GeoCube constructCube(Cube cube, ModelProperties properties, GeoBone bone) {
        return this.fallback.constructCube(cube, properties, bone);
    }

    private GeoCube buildPolyMeshCube(PolyMesh mesh, ModelProperties properties) {
        double[][][] polygons = mesh.polysUnion().union();
        double[] positions = mesh.positions();
        double[] uvs = mesh.uvs();
        double[] normals = mesh.normals();
        boolean normalized = Boolean.TRUE.equals(mesh.normalizedUVs());
        ArrayList<GeoQuad> quads = new ArrayList<GeoQuad>(polygons.length);
        for (double[][] polygon : polygons) {
            if (polygon.length < 3) continue;
            GeoVertex[] vertices = new GeoVertex[polygon.length == 3 ? 4 : polygon.length];
            Vector3f normal = null;
            for (int i = 0; i < polygon.length; ++i) {
                int positionIndex = (int)polygon[i][0] * 3;
                int normalIndex = (int)polygon[i][1] * 3;
                int uvIndex = (int)polygon[i][2] * 2;
                if (positionIndex + 2 >= positions.length || uvIndex + 1 >= uvs.length) {
                    vertices = null;
                    break;
                }
                float u = (float)uvs[uvIndex];
                float v = 1.0f - (float)uvs[uvIndex + 1];
                if (!normalized) {
                    u = (float)((double)u / properties.textureWidth());
                    v = 1.0f - (float)(uvs[uvIndex + 1] / properties.textureHeight());
                }
                vertices[i] = new GeoVertex(new Vector3f((float)(-positions[positionIndex] / 16.0), (float)(positions[positionIndex + 1] / 16.0), (float)(positions[positionIndex + 2] / 16.0)), u, v);
                if (normal != null || normalIndex + 2 >= normals.length) continue;
                normal = new Vector3f((float)(-normals[normalIndex]), (float)normals[normalIndex + 1], (float)normals[normalIndex + 2]);
            }
            if (vertices == null) continue;
            if (polygon.length == 3) {
                vertices[3] = vertices[2];
            }
            quads.add(new GeoQuad(vertices, normal == null ? PolyMeshBakedModelFactory.calculateNormal(vertices) : normal, Direction.NORTH));
        }
        return new GeoCube((GeoQuad[])quads.toArray(GeoQuad[]::new), Vec3.ZERO, Vec3.ZERO, Vec3.ZERO, 0.0, false);
    }

    private static Vector3f calculateNormal(GeoVertex[] vertices) {
        Vector3f edgeB;
        Vector3f edgeA = new Vector3f((Vector3fc)vertices[1].position()).sub((Vector3fc)vertices[0].position());
        Vector3f result = edgeA.cross((Vector3fc)(edgeB = new Vector3f((Vector3fc)vertices[2].position()).sub((Vector3fc)vertices[0].position())));
        return result.lengthSquared() == 0.0f ? new Vector3f(0.0f, 1.0f, 0.0f) : result.normalize();
    }

    private static double component(double[] values, int index) {
        return values != null && index < values.length ? values[index] : 0.0;
    }
}

