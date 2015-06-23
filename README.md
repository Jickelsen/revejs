# revejs

A [ClojureScript](https://github.com/clojure/clojurescript) game made using [Quil](http://quil.info/) and the [Brute](https://github.com/markmandel/brute) entity component system library. Uses [Figwheel](https://github.com/bhauman/lein-figwheel) for sweet interactive playtesting!

## Usage

### Get it up and running
Run `lein figwheel` and go to http://localhost:3449/. All changes will be reflected on save. Enter will reset the game state. In emacs you can use `cider-connect` to localhost:7888 to connect to the figwheel repl. To then start a cljs repl issue `(use 'figwheel-sidecar.repl-api)` followed by `(cljs-repl)`.

### Playing the game
Use WASD to control the red ship. V reverses time for the ship (position state undo!) and C fires the cannon.
Arrows control the blue ship with . (period) engaging reverse time and , (comma) firing the cannon.

Beware of friendly fire!

## Todo
This is one of my first nontrivial Clojure projects, and I already see many places where the code can be made cleaner. Composability could be improved and I should probably be using protocols and such neat things together with the records used for components. Performance could certainly be better and processing.js (which I'm using through Quil) [does not seem to be getting the support it deserves](https://github.com/quil/quil/issues/156). I'm going to have a look at how [chocolatier](https://github.com/alexkehayias/chocolatier) uses [records and protocols](http://alexkehayias.tumblr.com/post/78711349238/entity-component-model-in-clojurescript) for entities and components, and how pixi.js performs.

## License

Copyrigh © 2015 Jacob Michelsen 

Distributed under the Eclipse Public License either version 1.0 or any later version.
