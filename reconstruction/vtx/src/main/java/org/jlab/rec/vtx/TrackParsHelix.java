package org.jlab.rec.vtx;



import org.jlab.clas.tracking.kalmanfilter.Units;
import org.jlab.clas.tracking.trackrep.Helix;
import org.jlab.geom.prim.Point3D;
import org.jlab.geom.prim.Vector3D;

public class TrackParsHelix extends Helix {

    /**
     * @return the _id1
     */
    public int getId() {
        return _id;
    }

    /**
     * @param _id1 the _id1 to set
     */
    public void setId(int _id1) {
        this._id = _id1;
    }


    public TrackParsHelix(int i1, double x0, double y0, double z0,
                                       double px0, double py0, double pz0, 
                                       int q, double Bf, double xb, double yb) {
        _id = i1;
        this.setHelixParams(x0, y0, z0, px0, py0, pz0, q, Bf, xb, yb);
    }

    private int _id;               // id of the track
    private Helix _helix;          // track helix
    private double _phi0;       // azimuth at the DOCA
    private double _d_rho;
    private double _d_z;
    private double _tanL;
    private double _phi;
    private double _alpha;
    private double _kappa;
    
    private double _cosphidca ;
    private double _sinphidca ;

    private double _x0;  // the reference point = the beam spot as default; test secondary vtx using these coords
    private double _y0;  // 
    private double _z0;  // 
    
    private double _x;  // the vertex
    private double _y; 
    private double _z;  // 
    
    private double _px;  // the momentum at the vertex
    private double _py; 
    private double _pz;  // 
    

    public void setHelixParams(double x0, double y0, double z0,
                                       double px0, double py0, double pz0, 
                                       int q, double Bf, double xb, double yb) { 

        this._helix = new Helix(x0,y0,z0,px0,py0,pz0,q,Bf,xb,yb,Units.CM);
       /* 
        
        this._z0 = 0;
        this._pt = Math.sqrt(px0*px0+py0*py0);

        double xC = (1. / this._helix.getOmega()- this._helix.getD0()) * Math.sin(Math.atan2(py0, px0));
        double yC = (-1. / this._helix.getOmega() + this._helix.getD0()) * Math.cos(Math.atan2(py0, px0));
 
        this._phi_dca = Math.atan2(yC, xC);
        if (-q < 0) {
            this._phi_dca = Math.atan2(-yC, -xC);
        }
        _cosphidca = Math.cos(this._phi_dca);
        _sinphidca = Math.sin(this._phi_dca);

        */
       this._x0 = this._helix.getXb();
       this._y0 = this._helix.getYb();
       this._z0 =0;
       
       this._x = x0;
       this._y = y0;
       this._z = z0;
       this._px = px0;
       this._py = py0;
       this._pz = pz0;
       
        _alpha = 1/(this._helix.getB()*this._helix.getLightVelocity());
        // set kappa to define the charge
        _kappa = _alpha * this._helix.getOmega();
        
        this.updateHelix();
    } // end setHelixParams()


    private void updateFromHelix() {
            this._x = _x0 + this._d_rho * Math.cos(this._phi0) + this._alpha / this._kappa * (Math.cos(this._phi0) - Math.cos(this._phi0 + this._phi));
            this._y = _y0 + this._d_rho * Math.sin(this._phi0) + this._alpha / this._kappa * (Math.sin(this._phi0) - Math.sin(this._phi0 + this._phi));
            this._z = _z0 + this._d_z - this._alpha / this._kappa * this._tanL * this._phi;
            this._px = -Math.sin(this._phi0 + this._phi) / Math.abs(this._kappa);
            this._py = Math.cos(this._phi0 + this._phi) / Math.abs(this._kappa);
            this._pz = this._tanL / Math.abs(this._kappa);
        }

        private void updateHelix() {
            double kappa = Math.signum(this._kappa) / Math.sqrt(this._px * this._px + this._py * this._py);
            double tanL = Math.abs(kappa) * this._pz;
            double phit = Math.atan2(-this._px, this._py);
            double xcen = this._x + Math.signum(kappa) * this._alpha * this._py;
            double ycen = this._y - Math.signum(kappa) * this._alpha * this._px;
            double phi0 = Math.atan2(ycen - _y0, xcen - _x0);
            if (Math.signum(kappa) < 0) {
                phi0 = Math.atan2(-(ycen - _y0), -(xcen - _x0));
            }
            double phi = phit - phi0;
            if (Math.abs(phi) > Math.PI) {
                phi -= 2 * Math.signum(phi) * Math.PI;
            }
            double drho = (xcen - _x0) * Math.cos(phi0) + (ycen - _y0) * Math.sin(phi0) - this._alpha / kappa;
            double dz = this._z - _z0 + this._alpha / kappa * tanL * phi;
            this._d_rho = drho;
            this._phi0 = phi0;
            this._kappa = kappa;
            this._d_z = dz;
            this._tanL = tanL;
            this._phi = phi;
        }
        
    // calculate coordinates of the point of the helix curve from the parameter phi.
    // phi=0 corresponds to the ref. point given by the track
    public Point3D calcPoint(double phi) {
        this._phi = phi;
        this._x = _x0 + this._d_rho * Math.cos(this._phi0) + this._alpha / this._kappa * (Math.cos(this._phi0) - Math.cos(this._phi0 + this._phi));
        this._y = _y0 + this._d_rho * Math.sin(this._phi0) + this._alpha / this._kappa * (Math.sin(this._phi0) - Math.sin(this._phi0 + this._phi));
        this._z = _z0 + this._d_z - this._alpha / this._kappa * this._tanL * this._phi;
        return new Point3D(_x,_y,_z);

    }
    public Vector3D calcDir(double phi) {
        this._phi = phi;
        this._px = -Math.sin(this._phi0 + this._phi) / Math.abs(this._kappa);
        this._py = Math.cos(this._phi0 + this._phi) / Math.abs(this._kappa);
        this._pz = this._tanL / Math.abs(this._kappa);

        return new Vector3D(_px,_py,_pz).asUnit();
    }


}
