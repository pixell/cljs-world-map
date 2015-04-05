(ns vis-world-map)

(enable-console-print!)

(def width 960)
(def height 500)

(def d3 js/d3)

(def centroid
  (-> d3
      .-geo
      .path
      (.projection (fn [d] d))
      .-centroid))

(def projection
  (-> d3
      .-geo
      .orthographic
      (.scale 248)
      (.clipAngle 90)))

(def path
  (-> d3
      .-geo
      .path
      (.projection projection)))

(def graticule
  (-> d3
      .-geo
      .graticule
      (.extent #js [-180 -90]
               #js [179.9 89.9])))

(def svg
  (-> d3
      (.select "body")
      (.append "svg")
      (.attr #js {:width  width
                  :height height})))

(def line
  (-> svg
      (.append "path")
      (.datum graticule)
      (.attr "class" "graticule")
      (.attr "d" path)))

(def title
  (-> svg
      (.append "text")
      (.attr "x" (/ width 2))
      (.attr "y" (/ (* height 3) 5))))

(def radians (/ Math.PI 180))

(defn interpolator [rotation point]
  (let [x0  (* (first rotation) radians)
        cx0 (Math.cos x0)
        sx0 (Math.sin x0)
        y0  (* (second rotation) radians)
        cy0 (Math.cos y0)
        sy0 (Math.sin y0)
        kx0 (* cy0 cx0)
        ky0 (* cy0 sx0)

        x1  (* (- (first point)) radians)
        cx1 (Math.cos x1)
        sx1 (Math.sin x1)
        y1  (* (- (second point)) radians)
        cy1 (Math.cos y1)
        sy1 (Math.sin y1)
        kx1 (* cy1 cx1)
        ky1 (* cy1 sx1)

        d   (->>
             (+ (* sy0 sy1) (* cy0 cy1 (Math.cos (- x1 x0))))
             (Math.min 1)
             (Math.max -1)
             (Math.acos))
        k   (/ 1 (Math.sin d))]
    (fn [t]
      (let [td (* t d)
            B  (* (Math.sin td) k)
            A  (* (Math.sin (- d td)) k)
            x  (+ (* A kx0) (* B kx1))
            y  (+ (* A ky0) (* B ky1))
            z  (+ (* A sy0) (* B sy1))]
        #js [(/ (Math.atan2 y x) radians)
             (/ (Math.atan2 z (Math.sqrt (+ (* x x) (* y y)))) radians)]))))

(-> svg
    (.append "circle")
    (.attr "class" "graticule-outline")
    (.attr "cx" (/ width 2))
    (.attr "cy" (/ height 2))
    (.attr "r" (.scale projection)))

(defn country-transition [world]
  (let [countries (-> js/topojson
                      (.object world
                               (-> world
                                   .-objects
                                   .-countries))
                      (.-geometries))
        length  (.-length countries)
        country (-> svg
                    (.selectAll ".country")
                    (.data countries)
                    (.enter)
                    (.insert "path" ".graticule")
                    (.attr "class" "country")
                    (.attr "d" path))]
    (defn step [index]
      (.text title (.-id (nth countries index)))
      (-> country
          (.transition)
          (.style "fill" (fn [_ j] (if (= j index) "red" "#b8b8b8"))))
      (-> d3
          (.transition)
          (.delay 250)
          (.duration 1250)
          (.tween "rotate" (fn []
                             (let [rotation (.rotate projection)
                                   point    (centroid (nth countries index))]
                               (fn [t]
                                 (.rotate projection ((interpolator rotation point) t))
                                 (.attr country "d" path)
                                 (.attr line "d" path)))))
          (.transition)
          (.each "end" (fn [] (if (< index length)
                         (step (inc index))
                         (step 0))))))
    (step 0)))

(country-transition js/world)
