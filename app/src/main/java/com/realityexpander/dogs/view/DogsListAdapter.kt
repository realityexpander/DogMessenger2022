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
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.fragment_list.view.*
import kotlinx.android.synthetic.main.item_dog.view.*

class DogsListAdapter(val dogsList: ArrayList<DogBreed>) :
    RecyclerView.Adapter<DogsListAdapter.DogViewHolder>(),
    DogClickListener
{

    fun updateDogList(newDogsList: List<DogBreed>) {
        dogsList.clear()
        dogsList.addAll(newDogsList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = DataBindingUtil.inflate<ItemDogBinding>(inflater, R.layout.item_dog, parent, false)
        return DogViewHolder(view)
    }

    override fun getItemCount() = dogsList.size

    override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
        holder.view.dog = dogsList[position] // uses data bindingUtil - connects to variable "dog" / type "DogBreed" in XML
        holder.view.listener = this          // uses data bindingUtil - connects to variable "listener" / type "DogClickListener" in XML
    }

    override fun onDogClicked(v: View) {
        val uuid = v.dogId.text.toString().toInt()
        val action = ListFragmentDirections.actionDetailFragment()
        action.dogUuid = uuid
        Navigation.findNavController(v).navigate(action)
    }

    override fun onDogNameClicked(v: View) {
        val name = v.name.text.toString()
        val lifespan = (v.parent.parent as ViewGroup).findViewById<TextView>(R.id.lifespan).text.toString()
        // to look up a value in the dogsList array, the index needs to be set on a hidden layout TextView.
        Toast.makeText(v.context, "clicked on the dog named $name, $lifespan", Toast.LENGTH_SHORT).show()
    }

    class DogViewHolder(var view: ItemDogBinding) : RecyclerView.ViewHolder(view.root)
}