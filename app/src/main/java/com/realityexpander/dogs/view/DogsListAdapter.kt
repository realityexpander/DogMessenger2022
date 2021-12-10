package com.realityexpander.dogs.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.realityexpander.dogs.R
import com.realityexpander.dogs.databinding.ItemDogBinding
import com.realityexpander.dogs.model.DogBreed
//import kotlinx.android.synthetic.main.fragment_detail.view.*
//import kotlinx.android.synthetic.main.fragment_list.view.*
//import kotlinx.android.synthetic.main.item_dog.view.*

class DogsListAdapter(val dogsList: ArrayList<DogBreed>) :
    RecyclerView.Adapter<DogsListAdapter.DogViewHolder>(), DogClickListener {
    lateinit var bind: ItemDogBinding

    fun updateDogList(newDogsList: List<DogBreed>) {
        dogsList.clear()
        dogsList.addAll(newDogsList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        bind = ItemDogBinding.inflate(inflater, parent, false)

        return DogViewHolder(bind)
    }

    override fun getItemCount() = dogsList.size

    override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
        holder.view.dog = dogsList[position] // uses data bindingUtil - connects to variable "dog" / type "DogBreed" in XML
        holder.view.listener = this          // uses data bindingUtil - connects to variable "listener" / type "DogClickListener" in XML
        holder.view.position = position  // saves the position in a hidden view

        holder.bindViewToData(position)

//        // YET another way to setup click listeners and use resource binding efficiently
//        holder.view.listener2 = object: ClickListener {
//            override fun click(v: View) {
//                Toast.makeText(holder.view.root.context, "hello $position", Toast.LENGTH_SHORT).show()
//            }
//        }

        // AND ANOTHER way Uses the DogViewHolder for click() and ClickListener Interface
        holder.view.listener2 = holder as ClickListener
    }

    // original using just viewBinding - uses hidden view to store uuid/position of this item
//    override fun onDogClicked(v: View) {
//        val uuid = v.dogId.text.toString().toInt()
//        val action = ListFragmentDirections.actionDetailFragment()
//        action.dogUuid = uuid
//        Navigation.findNavController(v).navigate(action)
//    }

    // Using onClick @binding in the layout resource - NOTE must have position/uuid saved in a hidden textview in the layout (a lil clunky)
    override fun onDogClicked(v: View) {  // receives the entire LinearLayout ViewGroup
        val dog = dogsList[v.findViewById<TextView>(R.id.positionId).text.toString().toInt()]
        val uuid = dog.uuid
        val action = ListFragmentDirections.actionDetailFragment(dogUuid = uuid)
        Navigation.findNavController(v).navigate(action)
    }

    // Using onClick @binding from the layout item (textView in this case)
    override fun onDogNameClicked(v: View) {  // receives just the individual TextView
        Toast.makeText(v.context, "clicked on the dog named ${(v as TextView).text}", Toast.LENGTH_SHORT).show()
    }

    inner class DogViewHolder(var view: ItemDogBinding) : RecyclerView.ViewHolder(view.root), ClickListener {
        private val layout = bind.dogItemLayout  // this items' particular item View
        private val name = bind.name
        private val lifespan = bind.lifespan
        private var dog: DogBreed? = null

        // Bound without layout resources
        fun bindViewToData(position: Int) {

            name.setOnClickListener {
                Toast.makeText(view.root.context, "clicked on the dog named ${dogsList[position].dogBreed}", Toast.LENGTH_SHORT).show()
            }

            // Should not use the resource based nav
            layout.setOnClickListener { layoutView->
                val uuid = dogsList[position].uuid
                val action = ListFragmentDirections.actionDetailFragment(dogUuid = uuid)
                Navigation.findNavController(layoutView).navigate(action)
            }

            lifespan.setOnClickListener {
                Toast.makeText(view.root.context, "clicked on the dog with lifespan ${dogsList[position].lifeSpan}", Toast.LENGTH_SHORT).show()
            }

            dog = dogsList[position]
        }

        // From ClickListener interface resource definition lambda
        override fun click(v: View) {
            Toast.makeText(view.root.context, "Yo ${dog?.imageUrl}", Toast.LENGTH_SHORT).show()
        }
    }
}